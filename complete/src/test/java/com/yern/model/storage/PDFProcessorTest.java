package com.yern.model.storage;

import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.yern.service.storage.PDFProcessor;

public class PDFProcessorTest {
    private final PDFProcessor fileProcessor = new PDFProcessor();
    private PDFProcessor spy; 

    @BeforeEach
    public void setup() {
        this.spy = Mockito.spy(fileProcessor);
    }

    @Test 
    public void isValidMedaType_returnsTrue_whenMediaTypeIsPDF() {
        assertTrue(
            fileProcessor.isValidMediaType(MediaType.APPLICATION_PDF)
        );
    }

    @Test 
    public void isValidMedaType_returnsFalse_whenMediaTypeIsNotPDF() {
        assertFalse(
            fileProcessor.isValidMediaType(MediaType.APPLICATION_JSON)
        );
    }
}
