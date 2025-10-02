package com.yern.service.storage.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yern.model.storage.ErrorLog;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.repository.storage.FileRepository;
import com.yern.service.storage.StorageProvider;

@Service
public class FileService {
    private FileRepository fileRepository;
    // TODO: could be a factory based on db field 
    private StorageProvider storageProvider;
    private FileProcessorOrchestrator fileProcessor;

    public FileService(
        @Autowired FileRepository fileRepository,
        @Autowired StorageProvider storageProvider,
        @Autowired FileProcessorOrchestrator fileProcessor
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProvider;
        this.fileProcessor = fileProcessor;
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

    // TODO: figure out how to handle which bucket raw and not raw things go to
    public FileImpl uploadFile(Path localPath, String relatedClass) throws UploadFileException {
        if (!Files.exists(localPath)) {
            throw new UploadFileException("File: " + localPath + " does not exist.");
        }

        String targetPath = getRawPathForResource(localPath, relatedClass);
        try {
            storageProvider.uploadFile(localPath, targetPath);
            // TODO: move this into upload file method 
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
            throw new UploadFileException();
        }

        return returnFile;  
    }

    public void processFiles(Pageable pageable) {
        Page<FileImpl> files = getFilesToProcess(pageable);

        for (FileImpl file : files.getContent()) {
            try {
                processFile(file);
            } catch (IOException e) {
                // TODO: log this 
            }
        }
    }

    public void processFile(FileImpl file) throws IOException {
        Path localFile = getNewFilePath(file.getBasename()); 

        try {
            storageProvider.downloadFile(localFile, file.getRawPath());
            fileProcessor.processFile(localFile);
            storageProvider.uploadFile(localFile, file.getFormattedPath());

            updateProcessedFile(file);
        } catch (AssertionError | IOException e) {
            updateFailedFile(file, e);
        } finally {
            try {
                localFile.toFile().delete();
            } catch (Exception e) {}
        }
    }  
    
    public void updateProcessedFile(FileImpl file) {
        assert(file.getFormattedPath() != null);
        assert(!(file.getFormattedPath().isEmpty()));
        assert(storageProvider.fileExists(file.getFormattedPath()));

        file.setError(null);
        fileRepository.save(file);
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
    public String getFormattedPathForResource(Path localPath, String relatedClass) {
        String fileBasename = localPath.getFileName().toString();
        return (
            "yern-uploads/clean/" + relatedClass.toLowerCase() + "/" +  fileBasename
        );
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
