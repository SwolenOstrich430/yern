package com.yern.service.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yern.model.storage.FileImpl;
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

    /**
     * 1. Get pages to process 
     * 2. For each file:
     *      a. download the file 
     *      b. pass the file to processFile 
     * 3. Take the processed file and upload to storage
     * 4a. If no errors:
     *      i. Update the file's formatted path, set error to null
     * 4b. If errors:
     *      i. Update the file's error field 
     * @param pageable
     */
    public void processFiles(Pageable pageable) {
        Page<FileImpl> files = getFilesToProcess(pageable);

        for (FileImpl file : files.getContent()) {
            processFile(file);
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
            if (localFile.toFile() != null) {
                localFile.toFile().delete();
            }
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

    public Path getNewFilePath(String fileBasename) {
        String randomDirectory = UUID.randomUUID().toString();
        return Path.of("/bin/temp/" + randomDirectory + fileBasename);
    }

    private void cleanupFile(Optional<Path> file) {
        if (file.isPresent()) {
            Files.deleteIfExists(file.get());
        }
    }
}
