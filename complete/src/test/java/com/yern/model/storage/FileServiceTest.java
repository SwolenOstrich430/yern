package com.yern.model.storage;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.yern.repository.storage.FileRepository;
import com.yern.service.storage.FileProcessor;
import com.yern.service.storage.FileService;
import com.yern.service.storage.StorageProvider;

public class FileServiceTest {
    @MockitoBean
    private FileRepository fileRepository;
    @MockitoBean
    private StorageProvider storageProvider;
    @MockitoBean
    private FileProcessor fileProcessor; 

    private FileService service;

    @BeforeEach
    public void setup() {
        this.service = new FileService(
            fileRepository, storageProvider, fileProcessor
        );
    }
}
