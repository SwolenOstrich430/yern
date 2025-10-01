package com.yern.service.storage.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.http.MediaType;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PDFProcessor implements FileProcessor {

    private final List<MediaType> mediaTypes = List.of(
        MediaType.APPLICATION_PDF
    );

    public void processFile(
        Path filePath
    ) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            removeMetaData(document);
            removeJavaScript(document);
            removeEmbeddedFiles(document);
            removeAcroForm(document);
            // compression should be happening here by default 
            // could enable xref 
            document.save(filePath.toString());
        } 
    }

    public void removeMetaData(
        PDDocument document
    ) throws IOException {
        COSArray documentID = document.getDocument().getDocumentID();
        if (documentID != null) {
            documentID.clear();
        }

        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDMetadata metadata = catalog.getMetadata();
        if (metadata != null) {
            catalog.setMetadata(null); 
        }
    }

    public void removeJavaScript(PDDocument document) throws IOException {
        // Remove document-level JavaScript
        if (document.getDocumentCatalog().getOpenAction() instanceof PDActionJavaScript) {
            document.getDocumentCatalog().setOpenAction(null);
        }

        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm != null) {
            acroForm.flatten();
        }
    }

    public void removeEmbeddedFiles(PDDocument document) throws IOException {
        if (document.getDocumentCatalog().getNames() != null &&
            document.getDocumentCatalog().getNames().getEmbeddedFiles() != null) {
            document.getDocumentCatalog().getNames().setEmbeddedFiles(null);
        }
    }

    public void removeAcroForm(PDDocument document) throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        
        if (acroForm != null) {
            document.getDocumentCatalog().setAcroForm(null); 
        }
    }

    @Override
    public boolean isValidMediaType(MediaType mediaType) {
        return this.mediaTypes.contains(mediaType);
    }
}
