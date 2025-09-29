package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.MediaType;

import com.yern.service.storage.GenericFileProcessor;
import com.yern.service.storage.NotUniqueException;
import com.yern.exceptions.NotFoundException;
import com.yern.service.storage.FileProcessor;
import com.yern.service.storage.FileProcessorFactory;

public class GenericFileProcessorTest {
    private GenericFileProcessor processor;
    private GenericFileProcessor spy;
    private FileProcessorFactory factory;
    private List<FileProcessor> processors;
    private Path path;
    private MediaType mediaType;


    @BeforeEach
    public void setup() {
        if (processors == null) {
            processors = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                processors.add(Mockito.mock(FileProcessor.class));
            }
        }

        this.path = Mockito.mock(Path.class);
        this.factory = Mockito.mock(FileProcessorFactory.class);
        this.processor = new GenericFileProcessor(processors);
        this.spy = Mockito.spy(processor);
        this.mediaType = Mockito.mock(MediaType.class);
    }

    @Test 
    public void processFile_getsTheAssociatedProviderAndCallsProcessFile() throws NotFoundException, NotUniqueException, IOException {
        Path otherPath = Mockito.mock(Path.class);
        doReturn(processors.get(0)).when(spy).getProvider(path);
        when(processors.get(0).processFile(path)).thenReturn(otherPath);

        Path foundPath = spy.processFile(path);

        assertEquals(otherPath, foundPath);
        verify(
            processors.get(0),
            times(1)
        )
        .processFile(path);
    }

    @Test 
    public void hasValidMediaType_returnsTrue_ifAnyProvidersMediaTypesMatchesTheProvided() throws IOException {
        doReturn(mediaType).when(spy).getMediaType(path);
        doReturn(true).when(spy).hasValidMediaType(mediaType);
        assertTrue(spy.hasValidMediaType(path));
    }

    @Test 
    public void hasValidMediaType_returnsFalse_ifAnyProvidersMediaTypesMatchesTheProvided() throws IOException {
        doReturn(mediaType).when(spy).getMediaType(path);
        assertFalse(spy.hasValidMediaType(path));
    }

    // TODO: move all this into the factory 
    // TODO: define a member variable of factory 
    @Test 
    public void getProvider_returnsTheRelatedProvider_ifASingleMatchIsFound() throws IOException {
        doReturn(mediaType).when(spy).getMediaType(path);
        when(processors.get(0).hasValidMediaType(mediaType)).thenReturn(true);
        FileProcessor foundProvider = spy.getProvider(path);
        assertInstanceOf(FileProcessor.class, foundProvider);
    }

    @Test 
    public void getProvider_raisesIOException_ifAMatchIsNotFound() {
        assertThrows(
            IOException.class, 
            () -> processor.getProvider(path)
        );
    }

    @Test 
    public void getProvider_raisesNotUniqueException_ifMoreThanOneMatchIsFound() throws IOException {
        doReturn(mediaType).when(spy).getMediaType(path);
        when(processors.get(0).hasValidMediaType(mediaType)).thenReturn(true);
        when(processors.get(1).hasValidMediaType(mediaType)).thenReturn(true);
        
        assertThrows(
            NotUniqueException.class, 
            () -> spy.getProvider(path)
        );
    }

    @Test 
    public void getMediaType_returnsAMediaType_ifAMatchIsFound() throws IOException {
        String fileName = UUID.randomUUID().toString();
        when(path.toString()).thenReturn(fileName);

        try (MockedStatic<MediaTypeFactory> mockedStatic = mockStatic(MediaTypeFactory.class)) {
            mockedStatic.when(() -> MediaTypeFactory.getMediaType(fileName)).thenReturn(Optional.of(mediaType));

            MediaType foundType = processor.getMediaType(path);
            assertEquals(mediaType, foundType);
        }
    }

    @Test 
    public void getMediaType_throwsIOException_ifAMatchIsNotFound() {
        String fileName = UUID.randomUUID().toString();
        when(path.toString()).thenReturn(fileName);

        try (MockedStatic<MediaTypeFactory> mockedStatic = mockStatic(MediaTypeFactory.class)) {
            mockedStatic.when(() -> MediaTypeFactory.getMediaType(fileName)).thenReturn(Optional.empty());

             assertThrows(
            IOException.class, 
            () -> processor.getMediaType(path)
        );
        }
    }
}
