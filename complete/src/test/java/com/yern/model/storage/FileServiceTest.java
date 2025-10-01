package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.yern.exceptions.NotFoundException;
import com.yern.model.pattern.Section;
import com.yern.repository.storage.FileRepository;
import com.yern.service.storage.FileProcessorOrchestrator;
import com.yern.service.storage.FileService;
import com.yern.service.storage.GenericFileProcessor;
import com.yern.service.storage.NotUniqueException;
import com.yern.service.storage.StorageProvider;

public class FileServiceTest {
    private FileRepository fileRepository;
    private StorageProvider storageProvider;
    private FileProcessorOrchestrator fileProcessor; 
    private List<FileImpl> files;
    private Page<FileImpl> page;
    private Pageable pageable;
    private String targetPath;
    private Path localPath;

    private FileService service;
    private FileService spy;

    @BeforeEach
    public void setup() {
        this.targetPath = UUID.randomUUID().toString();
        this.localPath = mock(Path.class);
        this.fileRepository = Mockito.mock(FileRepository.class);
        this.storageProvider = Mockito.mock(StorageProvider.class);
        this.fileProcessor = Mockito.mock(GenericFileProcessor.class);
        
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
    public void uploadFile_uploadsFileToStorage() throws IOException, UploadFileException {
        doReturn(targetPath).when(spy).getRawPathForResource(
            localPath, Section.class.toString()
        );
        when(storageProvider.fileExists(targetPath)).thenReturn(true);

        spy.uploadFile(localPath, Section.class.toString());

        verify(
            storageProvider, 
            times(1)
        ).uploadFile(localPath, targetPath);
    }

    @Test 
    public void uploadFile_throwsUploadFileException_whenUploadFails() throws IOException {
        doThrow(
            IOException.class
        ).when(
            storageProvider
        ).uploadFile(localPath, targetPath);
        
        assertThrows(
            UploadFileException.class, 
            () -> service.uploadFile(localPath, Section.class.toString())
        );
    }

    @Test 
    public void uploadFile_throwsUploadFileException_whenFileDoesntExistAfterUpload() {
        when(storageProvider.fileExists(targetPath)).thenReturn(false);
        doReturn(targetPath).when(spy).getRawPathForResource(localPath, Section.class.toString());

        assertThrows(
            UploadFileException.class, 
            () -> spy.uploadFile(localPath, Section.class.toString())
        );

        verify(storageProvider, times(1)).fileExists(targetPath);
    }

    @Test 
    public void uploadFile_createsFileEntry_whenUploadSuccessful() throws UploadFileException, IOException {
        when(storageProvider.fileExists(targetPath)).thenReturn(true);

        try (MockedStatic<FileImpl> mockedStatic = mockStatic(FileImpl.class)) {
            mockedStatic.when(
                () -> FileImpl.from(targetPath)
            ).thenReturn(files.get(0));
            when(fileRepository.save(files.get(0))).thenReturn(files.get(0));
            doReturn(targetPath).when(spy).getRawPathForResource(localPath, Section.class.toString());
            
            spy.uploadFile(localPath, Section.class.toString());
            
            verify(
                fileRepository, 
                times(1)
            ).save(files.get(0));
        }
    }

    @Test 
    public void uploadFile_returnsTheCreatedFile_whenUploadSuccessful() throws UploadFileException {
        when(storageProvider.fileExists(targetPath)).thenReturn(true);
        doReturn(targetPath).when(spy).getRawPathForResource(localPath, Section.class.toString());

        try (MockedStatic<FileImpl> mockedStatic = mockStatic(FileImpl.class)) {
            mockedStatic.when(
                () -> FileImpl.from(targetPath)
            ).thenReturn(files.get(0));
            when(fileRepository.save(files.get(0))).thenReturn(files.get(0));

            FileImpl savedFile = spy.uploadFile(localPath, Section.class.toString());
            assertEquals(savedFile, files.get(0));
            
            verify(
                fileRepository, 
                times(1)
            ).save(files.get(0));
        }
    }

    @Test 
    public void uploadFile_throwsUploadFileException_whenDBInsertUnsuccessful() throws UploadFileException {
        when(storageProvider.fileExists(targetPath)).thenReturn(true);
        doReturn(targetPath).when(spy).getRawPathForResource(localPath, Section.class.toString());

        try (MockedStatic<FileImpl> mockedStatic = mockStatic(FileImpl.class)) {
            mockedStatic.when(
                () -> FileImpl.from(targetPath)
            ).thenReturn(files.get(0));
            when(fileRepository.save(files.get(0))).thenThrow(IllegalArgumentException.class);

            assertThrows(
                UploadFileException.class, 
                () -> spy.uploadFile(localPath, Section.class.toString())
            );
            
            verify(
                fileRepository, 
                times(1)
            ).save(files.get(0));
        }
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

    }

    @Test 
    public void processFile_downloadsTheRawVersionOfTheFile() throws IOException {
        when(files.get(0).getRawPath()).thenReturn(targetPath);
        doNothing().when(fileProcessor).processFile(localPath);
        doNothing().when(storageProvider).uploadFile(localPath, targetPath);
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);
        doNothing().when(spy).updateProcessedFile(files.get(0));

        spy.processFile(files.get(0));

        Mockito.verify(
            storageProvider, 
            Mockito.times(1)
        ).downloadFile(localPath, targetPath);
    }

    @Test 
    public void processFile_processesTheFile() throws IOException {
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(storageProvider).uploadFile(localPath, targetPath);
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);
        doNothing().when(spy).updateProcessedFile(files.get(0));

        spy.processFile(files.get(0));

        Mockito.verify(
            fileProcessor, 
            Mockito.times(1)
        ).processFile(localPath);
    }

    @Test 
    public void processFile_uploadsTheProcessedFileToTheFilesFormattedPath_ifThereAreNoErrors() throws NotFoundException, NotUniqueException, IOException {
        when(files.get(0).getFormattedPath()).thenReturn(targetPath);
        doNothing().when(fileProcessor).processFile(localPath);
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(spy).updateProcessedFile(files.get(0));
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);

        spy.processFile(files.get(0));

        Mockito.verify(
            storageProvider, 
            Mockito.times(1)
        ).uploadFile(localPath, targetPath);
    }

    @Test 
    public void processFile_updatesTheProcessedFile_ifThereAreNoErrors() throws NotFoundException, NotUniqueException, IOException {
        when(files.get(0).getFormattedPath()).thenReturn(targetPath);
        doNothing().when(fileProcessor).processFile(localPath);
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(storageProvider).uploadFile(localPath, targetPath);
        doNothing().when(spy).updateProcessedFile(files.get(0));

        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);

        spy.processFile(files.get(0));

        Mockito.verify(
            spy, 
            Mockito.times(1)
        ).updateProcessedFile(files.get(0));
    }

    @Test 
    public void processFile_updatesAFileAsFailed_ifAnIOExceptionIsThrown() throws IOException {
        doThrow(IOException.class).when(storageProvider).downloadFile(any(), any());
        spy.processFile(files.get(0));

        verify(
            spy, 
            times(1)
        )
        .updateFailedFile(
            eq(files.get(0)), 
            any(Throwable.class)
        );
    }

    @Test 
    public void updateProcessedFile_throwsException_ifFilesFormattedPathIsEmpty() {
        when(files.get(0).getFormattedPath()).thenReturn("");
        
        assertThrows(
            AssertionError.class,
            () -> service.updateProcessedFile(files.get(0))
        );
    }

    @Test 
    public void updateProcessedFile_throwsException_ifFormattedPathDoesntExist() {
        when(files.get(0).getFormattedPath()).thenReturn(targetPath);
        when(storageProvider.fileExists(targetPath)).thenReturn(false);

        assertThrows(
            AssertionError.class,
            () -> service.updateProcessedFile(files.get(0))
        );

        verify(storageProvider, times(1)).fileExists(targetPath);
    }

    @Test 
    public void updateProcessedFile_setsFilesErrorToNull() {
        when(files.get(0).getFormattedPath()).thenReturn(targetPath);
        when(storageProvider.fileExists(targetPath)).thenReturn(true);

        assertDoesNotThrow(
            () -> service.updateProcessedFile(files.get(0))
        );

        verify(storageProvider, times(1)).fileExists(targetPath);
        verify(files.get(0), times(1)).setError(null);
    }

    @Test 
    public void updateProcessedFile_savesTheFileToDB_ifNoErrors() {
        when(files.get(0).getFormattedPath()).thenReturn(targetPath);
        when(storageProvider.fileExists(targetPath)).thenReturn(true);

        assertDoesNotThrow(
            () -> service.updateProcessedFile(files.get(0))
        );

        verify(storageProvider, times(1)).fileExists(targetPath);
        verify(fileRepository, times(1)).save(files.get(0));
    }

    @Test 
    public void updateFailedFile_setsFileErrorToProvidedExc() {
        IOException exc = Mockito.mock(IOException.class);
        
        service.updateFailedFile(files.get(0), exc);

        verify(files.get(0), times(1)).setError(exc);
    }

    @Test 
    public void updateFailedFile_setsFilesFormattedPathToNull() {
        IOException exc = Mockito.mock(IOException.class);
        
        service.updateFailedFile(files.get(0), exc);

        verify(files.get(0), times(1)).setFormattedPath(null);
    }

    @Test 
    public void updateFailedFile_savesTheUpdatedFileToDB() {
        IOException exc = Mockito.mock(IOException.class);
        
        service.updateFailedFile(files.get(0), exc);

        verify(fileRepository, times(1)).save(files.get(0));
    }
} 
