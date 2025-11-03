package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;

import com.yern.common.file.FileUtil;
import com.yern.dto.storage.ProcessFileRequest;
import com.yern.model.pattern.Section;
import com.yern.repository.storage.FileRepository;
import com.yern.service.messaging.MessagePublisher;
import com.yern.service.storage.StorageProvider;
import com.yern.service.storage.file.FileService;
import com.yern.service.storage.file.access.FileAccessControlService;
import com.yern.service.storage.file.processing.FileProcessorOrchestrator;

@TestPropertySource(properties = {"messaging.topics.file-update=topic-name"})
public class FileServiceTest {
    @Mock 
    private FileRepository fileRepository;
    @Mock
    private StorageProvider storageProvider;
    @Mock
    private FileProcessorOrchestrator fileProcessor; 
    @Mock  
    private MessagePublisher publisher;
    @Mock 
    FileAccessControlService accessService;
    @Mock 
    ProcessFileRequest processFileRequest;

    @Value("${messaging.topics.file-update}") 
    String fileUpdateTopicName;

    private List<FileImpl> files;
    private Page<FileImpl> page;
    private Pageable pageable;
    private String targetPath;
    private Path localPath;

    private FileService service;
    private FileService spy;

    private final Long userId = 1L;
    private final String relatedClass = Section.class.getSimpleName();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        service = new FileService(
            fileRepository, 
            storageProvider, 
            fileProcessor, 
            publisher, 
            accessService,
            fileUpdateTopicName
        );
        spy = spy(service);

        this.targetPath = UUID.randomUUID().toString();
        this.localPath = mock(Path.class);
        this.files = new ArrayList<>();
        this.files.add(Mockito.mock(FileImpl.class));
        this.files.add(Mockito.mock(FileImpl.class));
        this.pageable = Mockito.mock(PageRequest.class);
        this.page = new PageImpl<>(files, pageable, files.size());
    }

    @Test 
    public void uploadAndSaveFile_validatesTheProvidedFileExists() throws UploadFileException, FileNotFoundException {
        doReturn(targetPath).when(spy).uploadFileRaw(localPath, relatedClass);
        doReturn(files.get(0)).when(spy).saveFile(userId, targetPath);
        doNothing().when(spy).sendUploadFileEvent(files.get(0));


        try (MockedStatic<FileUtil> mockedStatic = Mockito.mockStatic(FileUtil.class)) {
            mockedStatic.when(() -> FileUtil.validateFileExists(localPath)).thenAnswer(invocation -> null);
            
            spy.uploadAndSaveFile(userId, localPath, relatedClass);
            
            mockedStatic.verify(() -> FileUtil.validateFileExists(localPath));
        }
    }

    @Test 
    public void uploadAndSaveFile_uploadsTheFileToStorage() throws UploadFileException, FileNotFoundException {
        doReturn(targetPath).when(spy).uploadFileRaw(localPath, relatedClass);
        doReturn(files.get(0)).when(spy).saveFile(userId, targetPath);
        doNothing().when(spy).sendUploadFileEvent(files.get(0));

        try (MockedStatic<FileUtil> mockedStatic = Mockito.mockStatic(FileUtil.class)) {
            mockedStatic.when(() -> FileUtil.validateFileExists(localPath)).thenAnswer(invocation -> null);
            
            spy.uploadAndSaveFile(userId, localPath, relatedClass);
            
            verify(spy, times(1)).uploadFileRaw(localPath, relatedClass);
        }
    }

    @Test 
    public void uploadAndSaveFile_savesTheFileToDB() throws UploadFileException, FileNotFoundException {
        doReturn(targetPath).when(spy).uploadFileRaw(localPath, relatedClass);
        doReturn(files.get(0)).when(spy).saveFile(userId, targetPath);
        doNothing().when(spy).sendUploadFileEvent(files.get(0));

        try (MockedStatic<FileUtil> mockedStatic = Mockito.mockStatic(FileUtil.class)) {
            mockedStatic.when(() -> FileUtil.validateFileExists(localPath)).thenAnswer(invocation -> null);
            
            spy.uploadAndSaveFile(userId, localPath, relatedClass);
            
            verify(spy, times(1)).saveFile(userId, targetPath);
        }
    }

    @Test 
    public void uploadAndSaveFile_sendsFileUploadEvent() throws UploadFileException, FileNotFoundException {
        doReturn(targetPath).when(spy).uploadFileRaw(localPath, relatedClass);
        doReturn(files.get(0)).when(spy).saveFile(userId, targetPath);
        doNothing().when(spy).sendUploadFileEvent(files.get(0));

        try (MockedStatic<FileUtil> mockedStatic = Mockito.mockStatic(FileUtil.class)) {
            mockedStatic.when(() -> FileUtil.validateFileExists(localPath)).thenAnswer(invocation -> null);
            
            spy.uploadAndSaveFile(userId, localPath, relatedClass);
            
            verify(spy, times(1)).sendUploadFileEvent(files.get(0));
        }
    }

    @Test 
    public void uploadFileRaw_uploadsFileToStorage() throws UploadFileException, IOException {
        doReturn(targetPath).when(spy).getRawPathForResource(
            localPath, relatedClass
        );
        when(
            storageProvider.fileExists(targetPath)
        ).thenReturn(true);

        spy.uploadFileRaw(
            localPath, 
            relatedClass
        );

        verify(
            storageProvider, 
            times(1)
        ).uploadFile(localPath, targetPath);
    }

    @Test 
    public void uploadFileRaw_returnsThePathTheFileWasUploadedTo_ifFileExistsForProviderAfterUpload() throws UploadFileException, IOException {
        doReturn(targetPath).when(spy).getRawPathForResource(
            localPath, Section.class.getSimpleName()
        );
        when(
            storageProvider.fileExists(targetPath)
        ).thenReturn(true);

        String path = spy.uploadFileRaw(
            localPath, Section.class.getSimpleName()
        );

        assertEquals(path, targetPath);
    }

    @Test 
    public void uploadFileRaw_throwsFileUploadException_ifUploadFileIsUnsuccessful() {
        doReturn(targetPath).when(spy).getRawPathForResource(
            localPath, Section.class.getSimpleName()
        );
        when(
            storageProvider.fileExists(targetPath)
        ).thenReturn(false);

        assertThrows(
            UploadFileException.class,
            () -> spy.uploadFileRaw(
                localPath, Section.class.getSimpleName()
            )
        );
    }

    @Test 
    public void saveFile_convertsTargetPathToFileImpl_andSavesItToDB() throws AccessDeniedException, UploadFileException {
        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(files.get(0));

        when(files.get(0).getId()).thenReturn(1L);

        service.saveFile(userId, targetPath);
        
        verify(
            fileRepository, 
            times(1)
        ).save(any(FileImpl.class));
    }

    @Test 
    public void saveFile_raisesException_ifUploadFileFails() {
        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(null);

        assertThrows(
            UploadFileException.class, 
            () -> service.saveFile(userId, targetPath)
        );
        
        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(files.get(0));

        when(files.get(0).getId()).thenReturn(null);

        assertThrows(
            UploadFileException.class, 
            () -> service.saveFile(userId, targetPath)
        );

        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(files.get(0));

        when(files.get(0).getId()).thenReturn(0L);

        assertThrows(
            UploadFileException.class, 
            () -> service.saveFile(userId, targetPath)
        );
    }

    @Test 
    public void saveFile_setsTheProvidedUserId_asTheOwnerOfTheFileInDb() throws AccessDeniedException, UploadFileException {
        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(files.get(0));

        when(files.get(0).getId()).thenReturn(1L);

        service.saveFile(userId, targetPath);
        
        verify(
            accessService, 
            times(1)
        ).createOwner(userId, 1L);
    }

    @Test 
    public void saveFile_returnsTheCreatedFile_ifSaveAndAccessSetIsSuccessful() throws AccessDeniedException, UploadFileException {
        when(
            fileRepository.save((any(FileImpl.class)))
        ).thenReturn(files.get(0));

        when(files.get(0).getId()).thenReturn(1L);

        FileImpl file = service.saveFile(userId, targetPath);
        assertEquals(file, files.get(0));
    }

    @Test 
    public void sendUploadFileEvent_publishesProcessFileRequest_toFileUpdateTopic() {
        try (MockedStatic<ProcessFileRequest> mockedStatic = Mockito.mockStatic(ProcessFileRequest.class)) {
            mockedStatic.when(() -> ProcessFileRequest.from(files.get(0))).thenReturn(processFileRequest);
            service.sendUploadFileEvent(files.get(0));

            verify(
                publisher, 
                times(1)
            ).publishMessage(
                fileUpdateTopicName, 
                processFileRequest
            );
        }
    }

    // @Test
    // public void getFilesToProcess_returnsAPageOfFileImpl_whereRawPathIsNotNullAndFormattedPathIsNull() {
    //     Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(page);
        
    //     Page<FileImpl> foundPage = service.getFilesToProcess(pageable);
    //     assertInstanceOf(PageImpl.class, foundPage);
    // }

    // @Test 
    // public void getFilesToProcess_returnsAnEmptyPage_ifAllFilesAreProcessed() {
    //     Page<FileImpl> emptyPage =  new PageImpl<FileImpl>(
    //         new ArrayList<>(),
    //         pageable,
    //         0
    //     );
    //     Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(emptyPage);
        
    //     service.getFilesToProcess(pageable);
        
    //     Mockito.verify(
    //         fileRepository, 
    //         Mockito.times(1)
    //     )
    //     .getFilesToProcess(pageable);
    // }

    // @Test 
    // public void getFilesToProcess_returnsANonEmptyPage_thereAreFileToProcess() {
    //     Mockito.when(fileRepository.getFilesToProcess(pageable)).thenReturn(page);
        
    //     Page<FileImpl> foundPage = service.getFilesToProcess(pageable);
    //     assertEquals(page, foundPage);
    // }

    // @Test 
    // public void processFiles_processesEachFile_returnedByGetFilesToProcess() {

    // }

    @Test 
    public void processFile_downloadsTheRawVersionOfTheFile() throws IOException {
        when(files.get(0).getRawPath()).thenReturn(targetPath);
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(spy).uploadAndSaveProcessedFile(files.get(0), localPath, localPath);
        when(fileProcessor.processFile(localPath)).thenReturn(localPath);

        spy.processFile(files.get(0));

        Mockito.verify(
            storageProvider, 
            Mockito.times(1)
        ).downloadFile(localPath, targetPath);
    }

    @Test 
    public void processFile_processesTheFile() throws IOException {
        when(files.get(0).getRawPath()).thenReturn(targetPath);
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(spy).uploadAndSaveProcessedFile(files.get(0), localPath, localPath);
        when(fileProcessor.processFile(localPath)).thenReturn(localPath);

        spy.processFile(files.get(0));

        Mockito.verify(
            fileProcessor, 
            Mockito.times(1)
        ).processFile(localPath);
    }

    @Test 
    public void processFile_uploadsTheProcessedFileToTheFilesFormattedPath_ifThereAreNoErrors() throws IOException {
        when(files.get(0).getRawPath()).thenReturn(targetPath);
        doReturn(localPath).when(spy).getNewFilePath(targetPath);
        when(files.get(0).getBasename()).thenReturn(targetPath);
        doNothing().when(storageProvider).downloadFile(localPath, targetPath);
        doNothing().when(spy).uploadAndSaveProcessedFile(files.get(0), localPath, localPath);
        when(fileProcessor.processFile(localPath)).thenReturn(localPath);

        spy.processFile(files.get(0));

        Mockito.verify(
            spy, 
            Mockito.times(1)
        ).uploadAndSaveProcessedFile(files.get(0), localPath, localPath);
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
        Throwable thrw = mock(Throwable.class);

        when(exc.getCause()).thenReturn(thrw);
        service.updateFailedFile(files.get(0), exc);

        verify(files.get(0), times(1)).setError(any(ErrorLog.class));
    }

    @Test 
    public void updateFailedFile_setsFilesFormattedPathToNull() {
        IOException exc = Mockito.mock(IOException.class);
        Throwable thrw = mock(Throwable.class);

        when(exc.getCause()).thenReturn(thrw);
        service.updateFailedFile(files.get(0), exc);

        verify(files.get(0), times(1)).setFormattedPath(null);
    }

    @Test 
    public void updateFailedFile_savesTheUpdatedFileToDB() {
        IOException exc = Mockito.mock(IOException.class);
        Throwable thrw = mock(Throwable.class);

        when(exc.getCause()).thenReturn(thrw);
        service.updateFailedFile(files.get(0), exc);
        
        verify(fileRepository, times(1)).save(files.get(0));
    }
} 
