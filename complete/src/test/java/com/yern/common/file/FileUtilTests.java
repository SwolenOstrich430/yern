package com.yern.common.file;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class FileUtilTests {

    @Mock
    private Path localPath; 

    @Test 
    public void validateFileExists_doesNothing_ifFileExists() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
            mockedStatic.when(() -> Files.exists(localPath)).thenReturn(true);
            assertDoesNotThrow(
                () -> FileUtil.validateFileExists(localPath)
            );
        }
    }

    public void validateFileExists_throwsFileNotFoundException_ifFileDoesntExist() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
            mockedStatic.when(() -> Files.exists(localPath)).thenReturn(false);
            assertThrows(
                FileNotFoundException.class,
                () -> FileUtil.validateFileExists(localPath)
            );
        }
    }
}
