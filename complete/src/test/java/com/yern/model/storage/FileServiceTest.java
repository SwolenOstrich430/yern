package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.yern.repository.storage.FileRepository;
import com.yern.service.storage.FileProcessor;
import com.yern.service.storage.FileService;
import com.yern.service.storage.StorageProvider;

public class FileServiceTest {
    private FileRepository fileRepository;
    private StorageProvider storageProvider;
    private FileProcessor fileProcessor; 
    private List<FileImpl> files;
    private Page<FileImpl> page;
    private Pageable pageable;

    private FileService service;
    private FileService spy;

    @BeforeEach
    public void setup() {
        this.fileRepository = Mockito.mock(FileRepository.class);
        this.storageProvider = Mockito.mock(StorageProvider.class);
        this.fileProcessor = Mockito.mock(FileProcessor.class);
        
        this.files = new ArrayList<>();
        this.files.add(Mockito.mock(FileImpl.class));
        this.files.add(Mockito.mock(FileImpl.class));
        this.pageable = Mockito.mock(PageRequest.class);
        this.page = new PageImpl<>(files, pageable, files.size());

        this.service = new FileService(
            fileRepository, 
            storageProvider, 
            fileProcessor
        );

        this.spy = Mockito.spy(this.service);
    }

    @Test
    public void getFilesToProcess_returnsAPageOfFileImpl_whereRawPathIsNotNullAndFormattedPathIsNull() {
        Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(page);
        
        Page<FileImpl> foundPage = service.getFilesToProcess(pageable);
        assertInstanceOf(PageImpl.class, foundPage);
    }

    @Test 
    public void getFilesToProcess_returnsAnEmptyPage_ifAllFilesAreProcessed() {
        Page<FileImpl> emptyPage =  new PageImpl<FileImpl>(
            new ArrayList<>(),
            pageable,
            0
        );
        Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(emptyPage);
        
        service.getFilesToProcess(pageable);
        
        Mockito.verify(
            fileRepository, 
            Mockito.times(1)
        )
        .getFilesToProcess(pageable);
    }

    @Test 
    public void getFilesToProcess_returnsANonEmptyPage_thereAreFileToProcess() {
        Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(page);
        
        Page<FileImpl> foundPage = service.getFilesToProcess(pageable);
        assertEquals(page, foundPage);
    }

    @Test 
    public void processFiles_processesEachFile_returnedByGetFilesToProcess() {
        Mockito.doReturn(page).when(spy).getFilesToProcess(pageable);

        spy.processFiles(pageable);

        for (FileImpl file : files) {
            Mockito.verify(
                fileProcessor, 
                Mockito.times(1)
            ).processFile(file);
        }
    }
}
