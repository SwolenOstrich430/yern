package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FileImplTest {

    private FileImpl file; 
    private ProcessFileException error;

    @BeforeEach
    public void setup() {
        file = new FileImpl();
        error = Mockito.mock(ProcessFileException.class);
    }

    @Test 
    public void from_returnsAFileImpl_withARawPath() {
        String path = UUID.randomUUID().toString();
        FileImpl file = FileImpl.from(path);
        assertEquals(file.getRawPath(), path);
    }

    @Test
    public void hasError_returnsTrue_whenErrorIsNotNull() {
        file.setError(new ErrorLog());
        assertTrue(file.hasError());
    }

    @Test
    public void hasError_returnsFalse_whenErrorIsNotNull() {
        file.setError(null);
        assertFalse(file.hasError());
    }
}
