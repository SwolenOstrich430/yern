package com.yern.service.storage.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yern.common.file.FileUtil;
import com.yern.dto.storage.GrantFileAccessRequest;
import com.yern.dto.storage.GrantFileAccessResponse;
import com.yern.dto.storage.ProcessFileRequest;
import com.yern.exceptions.NotFoundException;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.RoleType;
import com.yern.model.storage.ErrorLog;
import com.yern.model.storage.FileAccessControl;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.repository.storage.FileRepository;
import com.yern.service.messaging.MessagePublisher;
import com.yern.service.storage.StorageProvider;
import com.yern.service.storage.file.access.FileAccessControlService;
import com.yern.service.storage.file.processing.FileProcessorOrchestrator;
import com.yern.service.user.UserService;

import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TODO: share file with other users 
 * TODO: allow other users to edit files
 * TODO: allow users to transfer ownership of files 
 * TODO: change section class to resource type enum 
 */
@Service
public class FileService {
    private FileRepository fileRepository;
    // TODO: could be a factory based on db field 
    private StorageProvider storageProvider;
    private FileProcessorOrchestrator fileProcessor;
    private MessagePublisher messagePublisher;
    private FileAccessControlService accessService;
    private UserService userService;
    private String fileUpdateTopicName; 


    public FileService(
        @Autowired FileRepository fileRepository,
        @Autowired StorageProvider storageProvider,
        @Autowired FileProcessorOrchestrator fileProcessor,
        @Autowired MessagePublisher messagePublisher,
        @Autowired FileAccessControlService accessService,
        @Autowired UserService userService,
        @Value("${messaging.topics.file-update}") String topicName
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProvider;
        this.fileProcessor = fileProcessor;
        this.messagePublisher = messagePublisher;
        this.accessService = accessService;
        this.userService = userService;
        this.fileUpdateTopicName = topicName;
    }

    // TODO: change related class to an actual class
    // TODO: or do resource type
    public FileImpl uploadAndSaveFile(
        Long userId,
        MultipartFile file, 
        String relatedClass
    ) throws UploadFileException {
        Path filePath = null;
        FileImpl uploadedFile = null; 

        try {
            // TODO: move this to FileUtils and rename 
            filePath = getNewFilePath(file.getOriginalFilename());
            File targetFile = filePath.toFile();
            file.transferTo(targetFile);
            assert(targetFile.exists() && file.getSize() > 0);
            
            uploadedFile = uploadAndSaveFile(userId, filePath, relatedClass);
        } catch(IOException | AssertionError exc) {
            throw new UploadFileException();
        }

        return uploadedFile;
    }

    // TODO: add OWNER entry in uploadFile 
    // TODO: verify OWNER entry was created successfully
    public FileImpl uploadAndSaveFile(
        Long userId, 
        Path localPath, 
        String relatedClass
    ) throws UploadFileException, IOException {
        FileUtil.validateFileExists(localPath);
        String targetPath = uploadFileRaw(localPath, relatedClass);
        FileImpl returnFile = saveFile(
            userId, targetPath, localPath.getFileName().toString()
        );
        sendUploadFileEvent(returnFile);
        
        return returnFile;  
    }

    // TODO: add unit tests
    public GrantFileAccessResponse grantAccess(
        Long fileOwnerId,
        GrantFileAccessRequest req
    ) throws Exception {
        userService.validateUser(req.getUserId());

        Optional<FileAccessControl> access = accessService.grantAccess(
            fileOwnerId, req
        );
        // TODO: change to uniform exception
        if (access.isEmpty()) {
            throw new Exception("Unable to create access to file");
        }

        return GrantFileAccessResponse.from(req, access.get());
    }

    public void streamFileContent(
        Long userId, 
        Long fileId, 
        HttpServletResponse response
    ) throws IOException {
        FileImpl file = getFileById(userId, fileId);
        // if the file hasn't been processed yet, we can't serve it
        if (!storageProvider.fileExists(file.getFormattedPath())) {
            sendUploadFileEvent(file);
            throw new NotFoundException(
                "File has not been processed yet. Please try again later."
            );
        }

        Path localFile = getNewFilePath(file.getBasename()); 
        storageProvider.downloadFile(localFile, file.getFormattedPath());
        assert(localFile.toFile().exists());

        InputStream inputStream = Files.newInputStream(localFile); 
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
        // TODO: add finally block to make sure this gets deleted
        FileUtils.delete(localFile.toFile());
    }

    public FileImpl getFileById(Long userId, Long fileId) {
        FileImpl file = fileRepository.getFileById(fileId);
        Assert.notNull(file);
        accessService.verifyAccess(userId, fileId, Permission.READ);

        return file;
    }

    public String uploadFileRaw(
        Path localPath, 
        String relatedClass
    ) throws UploadFileException, IOException {
        String targetPath = getRawPathForResource(
            localPath, relatedClass
        );
        
        fileProcessor.validateMediaType(localPath);
        
        try {
            storageProvider.uploadFile(localPath, targetPath);
            if (!storageProvider.fileExists(targetPath)) {
                throw new UploadFileException();
            }
        } catch(IOException exc) {
            // TODO: add logging 
            // TODO: create standard message for exception 
            throw new UploadFileException();
        }

        return targetPath;
    }

    public FileImpl saveFile(
        Long userId,
        String targetPath,
        String originalPath
    ) throws AccessDeniedException, UploadFileException {
        FileImpl createdFile = fileRepository.save(
            FileImpl.from(targetPath, originalPath)
        );

        if (createdFile == null || createdFile.getId() == null || 
            createdFile.getId() <= 0) {
            throw new UploadFileException();
        }

        accessService.createOwner(userId, createdFile.getId());
        return createdFile;
    }

    public void sendUploadFileEvent(FileImpl file) {
        ProcessFileRequest req = ProcessFileRequest.from(file);
        messagePublisher.publishMessage(fileUpdateTopicName, req);
    }

    public void processFiles(Pageable pageable) {
        Page<FileImpl> files = getFilesToProcess(pageable);

        for (FileImpl file : files.getContent()) {
            processFile(file);
        }
    }

    public void processFiles(List<ProcessFileRequest> reqs) {
        for (ProcessFileRequest req : reqs) {
            processFile(req);
        }
    }

    public void processFile(ProcessFileRequest req) {
        Assert.notNull(req);
        Assert.notNull(req.getFileId());

        FileImpl file = fileRepository.getFileById(req.getFileId());
        Assert.notNull(file);
        Assert.notNull(file.getStorageProvider());

        processFile(file);
    }

    // TODO: add some reporting on matching etag
    // TODO: set the public url
    public void processFile(FileImpl file) {
        Path localFile = null;
        
        try {
            localFile = getNewFilePath(file.getBasename()); 
            storageProvider.downloadFile(localFile, file.getRawPath());
            Path processedFile = fileProcessor.processFile(localFile);

            uploadAndSaveProcessedFile(file, localFile, processedFile);
        } catch (Exception e) {
            updateFailedFile(file, e);
        } finally {
            try {
                if (localFile instanceof Path) {
                    localFile.toFile().delete();
                }
            } catch (Exception e) {}
        }
    }  

    public void uploadAndSaveProcessedFile(
        FileImpl file,
        Path localFile,
        Path processedFile
    ) throws IOException {
        String formattedName = file.getFormattedPath();
        assert(formattedName != null && !formattedName.isEmpty());

        calculateEtag(localFile)
            .thenAccept(
                etag -> {
                    file.setEtag(etag);
                    try {
                        storageProvider.uploadFile(
                            processedFile, file.getFormattedPath()
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(
                            "Unable to load processed file to storage",
                            e
                        );
                    }

                    try {
                        file.setPublicUrl(storageProvider.getPublicUrl(
                            file.getFormattedPath()
                        ));
                    } catch(Exception exc) {
                        throw new RuntimeException(
                            "Unable to generate public url from file",
                            exc
                        );
                    }
                    
                    updateProcessedFile(file);
                }
            );
    }
    
    public void updateProcessedFile(FileImpl file) {
        assert(file.getFormattedPath() != null);
        assert(!(file.getFormattedPath().isEmpty()));
        assert(storageProvider.fileExists(file.getFormattedPath()));

        file.setError(null);
        fileRepository.save(file);
    }

    public CompletableFuture<String> calculateEtag(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: move to instance variable/separate service 
                MessageDigest md = MessageDigest.getInstance("SHA-256");

                // TODO: make buffer size configurable 
                byte[] buffer = new byte[8192]; // 8KB buffer
                try (InputStream is = Files.newInputStream(filePath)) {
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        md.update(buffer, 0, bytesRead);
                    }
                }

                byte[] hashBytes = md.digest();
                return HexFormat.of().formatHex(hashBytes);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to calculate ETag for file: " + filePath, e
                );
            }
        });
    }

    public void updateFailedFile(FileImpl file, Throwable exc) {
        file.setError(ErrorLog.from(exc));
        file.setFormattedPath(null);
        fileRepository.save(file);
    }

    // TODO: move to FileImpl class
    // TODO: add unit tests once moved 
    public String getRawPathForResource(Path localPath, String relatedClass) {
        String uniqueFileName = FileUtil.getUniqueFileName(localPath);

        return (
            "yern-uploads/raw/" + relatedClass.toLowerCase() + "/" +  uniqueFileName
        );
    }

    // TODO: not sure class name is the best for this?
    public String getFormattedPathForResource(FileImpl file) {
        return file.getFormattedPath();
    }

    public Page<FileImpl> getFilesToProcess(Pageable pageable) {
        return fileRepository.getFilesToProcess(pageable);
    }

    public Path getNewFilePath(String fileBasename) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fullPath = tempDir + fileBasename;
        File file = new File(fullPath);

        file.createNewFile();
        file.deleteOnExit();

        return Path.of(fullPath);
    }
}
