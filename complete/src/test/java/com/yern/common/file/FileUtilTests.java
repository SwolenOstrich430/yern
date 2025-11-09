package com.yern.common.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class FileUtilTests {

    @Mock
    private Path localPath; 
    @Mock 
    private Path otherPath;

    private String fileName;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        fileName = "test.txt";
    }

    @Test 
    public void validateFileExists_doesNothing_ifFileExists() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
            mockedStatic.when(() -> Files.exists(localPath)).thenReturn(true);
            assertDoesNotThrow(
                () -> FileUtil.validateFileExists(localPath)
            );
        }
    }

    @Test
    public void validateFileExists_throwsFileNotFoundException_ifFileDoesntExist() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
            mockedStatic.when(() -> Files.exists(localPath)).thenReturn(false);
            assertThrows(
                FileNotFoundException.class,
                () -> FileUtil.validateFileExists(localPath)
            );
        }
    }

    @Test 
    public void getUniqueFileName_returnsUniqueFileName_withMatchingExtension() {
        when(localPath.getFileName()).thenReturn(otherPath);
        when(otherPath.toString()).thenReturn(fileName);

        String fileName = FileUtil.getUniqueFileName(localPath);
        String[] filePaths = fileName.split("\\.");

        assertEquals(filePaths.length, 2);
        assertDoesNotThrow(() -> UUID.fromString(filePaths[0]));
        assertEquals(filePaths[1], "txt");
    }
}
