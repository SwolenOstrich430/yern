package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.yern.repository.storage.FileRepository;
import com.yern.service.storage.FileProcessor;
import com.yern.service.storage.FileService;
import com.yern.service.storage.StorageProvider;

public class FileServiceTest {
    private FileRepository fileRepository;
    private StorageProvider storageProvider;
    private FileProcessor fileProcessor; 

    private FileService service;

    @BeforeEach
    public void setup() {
        this.fileRepository = Mockito.mock(FileRepository.class);
        this.storageProvider = Mockito.mock(StorageProvider.class);
        this.fileProcessor = Mockito.mock(FileProcessor.class);

        this.service = new FileService(
            fileRepository, 
            storageProvider, 
            fileProcessor
        );
    }

    @Test
    public void getFilesToProcess_returnsAListOfFileImpl_whereRawPathIsNotNullAndFormattedPathIsNull() {
        List<FileImpl> files = service.getFilesToProcess();
        assertInstanceOf(List.class, files);
    }

    @Test public void 
    getFilesToProcess_returnsAnEmptyList_ifAllFilesAreProcessed() {
        Mockito.when(fileRepository.getFilesToProcess()).thenReturn(new ArrayList<>());
        
        service.getFilesToProcess();
        
        Mockito.verify(
            fileRepository, 
            Mockito.times(1)
        )
        .getFilesToProcess();
    }
}
