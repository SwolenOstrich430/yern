package com.yern.model.storage;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.yern.service.storage.PDFProcessor;

public class PDFProcessorTest {
    private final PDFProcessor fileProcessor = new PDFProcessor();
    private PDFProcessor spy; 
    private PDDocument document;
    private Path path; 
    private File file;
    private COSDocument cosDocument; 
    private COSArray documentId;
    private PDDocumentCatalog catalog;
    private PDMetadata metadata;

    @BeforeEach
    public void setup() {
        this.spy = Mockito.spy(fileProcessor);
        this.document = Mockito.mock(PDDocument.class);
        this.path = Mockito.mock(Path.class);
        this.file = Mockito.mock(File.class);
        this.cosDocument = Mockito.mock(COSDocument.class);
        this.documentId = Mockito.mock(COSArray.class);
        this.catalog = Mockito.mock(PDDocumentCatalog.class);
        this.metadata = Mockito.mock(PDMetadata.class);
    }

    @Test 
    public void processFile_removesMetaData() throws IOException {
        try (MockedStatic<Loader> mockedStatic = mockStatic(Loader.class)) {
            when(path.toFile()).thenReturn(file);
            mockedStatic.when(() -> Loader.loadPDF(file)).thenReturn(document);
            doNothing().when(spy).removeMetaData(document);
            doNothing().when(spy).removeJavaScript(document);
            doNothing().when(spy).removeEmbeddedFiles(document);
            doNothing().when(spy).flattenFormFields(document);
            
            spy.processFile(path);

            verify(spy, times(1)).removeMetaData(document);
        }
    }

    @Test 
    public void processFile_removesJavaScript() throws IOException {
        try (MockedStatic<Loader> mockedStatic = mockStatic(Loader.class)) {
            when(path.toFile()).thenReturn(file);
            mockedStatic.when(() -> Loader.loadPDF(file)).thenReturn(document);
            doNothing().when(spy).removeMetaData(document);
            doNothing().when(spy).removeJavaScript(document);
            doNothing().when(spy).removeEmbeddedFiles(document);
            doNothing().when(spy).flattenFormFields(document);
            
            spy.processFile(path);

            verify(spy, times(1)).removeJavaScript(document);
        }
    }

    @Test 
    public void processFile_removesEmbeddedFiles() throws IOException {
        try (MockedStatic<Loader> mockedStatic = mockStatic(Loader.class)) {
            when(path.toFile()).thenReturn(file);
            mockedStatic.when(() -> Loader.loadPDF(file)).thenReturn(document);
            doNothing().when(spy).removeMetaData(document);
            doNothing().when(spy).removeJavaScript(document);
            doNothing().when(spy).removeEmbeddedFiles(document);
            doNothing().when(spy).flattenFormFields(document);
            
            spy.processFile(path);

            verify(spy, times(1)).removeEmbeddedFiles(document);
        }
    }

    @Test 
    public void processFile_flattendFormFields() throws IOException {
        try (MockedStatic<Loader> mockedStatic = mockStatic(Loader.class)) {
            when(path.toFile()).thenReturn(file);
            mockedStatic.when(() -> Loader.loadPDF(file)).thenReturn(document);
            doNothing().when(spy).removeMetaData(document);
            doNothing().when(spy).removeJavaScript(document);
            doNothing().when(spy).removeEmbeddedFiles(document);
            doNothing().when(spy).flattenFormFields(document);
            
            spy.processFile(path);

            verify(spy, times(1)).flattenFormFields(document);
        }
    }

    @Test 
    public void processFile_savesTheConvertedDocumentToTheSuppliedPath() throws IOException {
        try (MockedStatic<Loader> mockedStatic = mockStatic(Loader.class)) {
            when(path.toFile()).thenReturn(file);
            mockedStatic.when(() -> Loader.loadPDF(file)).thenReturn(document);
            doNothing().when(spy).removeMetaData(document);
            doNothing().when(spy).removeJavaScript(document);
            doNothing().when(spy).removeEmbeddedFiles(document);
            doNothing().when(spy).flattenFormFields(document);
            
            spy.processFile(path);

            verify(document, times(1)).save(path.toString());
        }
    }

    @Test 
    public void removeMetaData_clearsDocumentId_ifOneExists() throws IOException {
        when(document.getDocument()).thenReturn(cosDocument);
        when(cosDocument.getDocumentID()).thenReturn(documentId);
        when(document.getDocumentCatalog()).thenReturn(catalog);
        when(catalog.getMetadata()).thenReturn(null);

        fileProcessor.removeMetaData(document);

        verify(documentId, times(1)).clear();
        
    }

    @Test 
    public void removeMetaData_doesntClearDocumentId_ifOneDoesntExists() throws IOException {
        when(document.getDocument()).thenReturn(cosDocument);
        when(cosDocument.getDocumentID()).thenReturn(null);
        when(document.getDocumentCatalog()).thenReturn(catalog);
        when(catalog.getMetadata()).thenReturn(null);

        fileProcessor.removeMetaData(document);

        verify(documentId, times(0)).clear();
    }

    @Test 
    public void removeMetaData_setsDocumentCatalogMetaDataToNull_ifOneExists() throws IOException {
        when(document.getDocument()).thenReturn(cosDocument);
        when(cosDocument.getDocumentID()).thenReturn(null);
        when(document.getDocumentCatalog()).thenReturn(catalog);
        when(catalog.getMetadata()).thenReturn(metadata);

        fileProcessor.removeMetaData(document);

        verify(catalog, times(1)).setMetadata(null);
    }

    @Test 
    public void removeMetaData_doesntSetDocumentCatalogMetaDataToNull_ifDoesntOneExists() throws IOException {
        when(document.getDocument()).thenReturn(cosDocument);
        when(cosDocument.getDocumentID()).thenReturn(null);
        when(document.getDocumentCatalog()).thenReturn(catalog);
        when(catalog.getMetadata()).thenReturn(null);

        fileProcessor.removeMetaData(document);

        verify(catalog, times(0)).setMetadata(null);
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
