package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yern.model.storage.FileImpl;
import com.yern.model.storage.UploadFileException;
import com.yern.repository.storage.FileRepository;

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

    // TODO: figure out how to handle which bucket raw and not raw things go to
    public FileImpl uploadFile(Path localPath, String targetPath) throws UploadFileException {
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
            processFile(file);
        }
    }

    public void processFile(FileImpl file) {
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
        file.setError(exc);
        file.setFormattedPath(null);
        fileRepository.save(file);
    }

    public Page<FileImpl> getFilesToProcess(Pageable pageable) {
        return fileRepository.getFilesToProcess(pageable);
    }

    // TODO: don't hardcode 
    public Path getNewFilePath(String fileBasename) {
        String randomDirectory = UUID.randomUUID().toString();
        return Path.of("/bin/temp/" + randomDirectory + fileBasename);
    }
}
