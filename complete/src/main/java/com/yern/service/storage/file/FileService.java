package com.yern.service.storage.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yern.dto.storage.ProcessFileRequest;
import com.yern.model.storage.ErrorLog;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.repository.storage.FileRepository;
import com.yern.service.messaging.MessagePublisher;
import com.yern.service.storage.StorageProvider;

import io.jsonwebtoken.lang.Assert;

@Service
public class FileService {
    private FileRepository fileRepository;
    // TODO: could be a factory based on db field 
    private StorageProvider storageProvider;
    private FileProcessorOrchestrator fileProcessor;
    private MessagePublisher messagePublisher;
    private String fileUpdateTopicName; 


    public FileService(
        @Autowired FileRepository fileRepository,
        @Autowired StorageProvider storageProvider,
        @Autowired FileProcessorOrchestrator fileProcessor,
        @Autowired MessagePublisher messagePublisher,
        @Value("${messaging.topics.file-update}") 
        String fileUpdateTopicName
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProvider;
        this.fileProcessor = fileProcessor;
        this.messagePublisher = messagePublisher;
        this.fileUpdateTopicName = fileUpdateTopicName;
    }

    public FileImpl uploadFile(MultipartFile file, String relatedClass) throws UploadFileException {
        Path filePath = null;
        FileImpl uploadedFile = null; 

        try {
            filePath = getNewFilePath(file.getOriginalFilename());
            File targetFile = filePath.toFile();
            file.transferTo(targetFile);
            assert(targetFile.exists() && file.getSize() > 0);
            
            uploadedFile = uploadFile(filePath, relatedClass);
        } catch(IOException | AssertionError exc) {
            throw new UploadFileException();
        }

        return uploadedFile;
    }

    public FileImpl uploadFile(Path localPath, String relatedClass) throws UploadFileException {
        if (!Files.exists(localPath)) {
            throw new UploadFileException("File: " + localPath + " does not exist.");
        }

        String targetPath = getRawPathForResource(localPath, relatedClass);
        try {
            storageProvider.uploadFile(localPath, targetPath);
            assert(storageProvider.fileExists(targetPath));
        } catch(IOException | AssertionError exc) {
            // TODO: add logging 
            // TODO: create standard message for exception 
            throw new UploadFileException();
        }

        FileImpl returnFile; 
        try {
            returnFile = fileRepository.save(FileImpl.from(targetPath));
        } catch(Exception exc) {
            // TODO: delete the file?
            throw new UploadFileException();
        }

        sendUploadFileEvent(returnFile);
        return returnFile;  
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

    // TODO: set the etag 
    // TODO: add some reporting on matching etag
    // TODO: set the public url
    public void processFile(FileImpl file) {
        Path localFile = null;
        
        try {
            localFile = getNewFilePath(file.getBasename()); 
            storageProvider.downloadFile(localFile, file.getRawPath());
            Path processedFile = fileProcessor.processFile(localFile);
            
            uploadAndSaveProcessedFile(file, localFile, processedFile);
        } catch (AssertionError | IOException e) {
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

    // TODO: not sure class name is the best for this?
    public String getRawPathForResource(Path localPath, String relatedClass) {
        String fileBasename = localPath.getFileName().toString();
        return (
            "yern-uploads/raw/" + relatedClass.toLowerCase() + "/" +  fileBasename
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

    // TODO: add unit test 
    // TODO: add file_permissions table and define permissions 
    public void validateAccess(
        Long fileId,
        Long userId
    ) throws AccessDeniedException {
        Optional<FileImpl> file = fileRepository.findById(fileId);
        file.orElseThrow(
            () -> new AccessDeniedException("File: " + fileId)
        );
    }
}
