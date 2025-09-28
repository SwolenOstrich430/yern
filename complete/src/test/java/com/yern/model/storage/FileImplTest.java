package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;

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
    public void hasError_returnsTrue_whenErrorIsNotNull() {
        file.setError(error);
        assertTrue(file.hasError());
    }

    @Test
    public void hasError_returnsFalse_whenErrorIsNotNull() {
        file.setError(null);
        assertFalse(file.hasError());
    }
}
