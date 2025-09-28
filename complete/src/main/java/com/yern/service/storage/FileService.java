package com.yern.service.storage;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yern.model.storage.FileImpl;
import com.yern.repository.storage.FileRepository;

@Service
public class FileService {
    private FileRepository fileRepository;
    private StorageProvider storageProvider;
    private FileProcessor fileProcessor;

    public FileService(
        @Autowired FileRepository fileRepository,
        @Autowired StorageProvider storageProvider,
        @Autowired FileProcessor fileProcessor
    ) {
        this.fileRepository = fileRepository;
        this.storageProvider = storageProvider;
        this.fileProcessor = fileProcessor;
    }

    public Page<FileImpl> getFilesToProcess(Pageable pageable) {
        return fileRepository.getFilesToProcess(pageable);
    }
}
