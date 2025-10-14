package com.yern.service.storage.file.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@Service 
@NoArgsConstructor
public class PDFProcessor implements FileProcessor {

    private final List<MediaType> mediaTypes = List.of(
        MediaType.APPLICATION_PDF
    );

    public Path processFile(Path filePath, Path targetPath) throws IOException {
        loadPdfAsync(filePath)
            .thenAccept(document -> {
                try {
                    processFile(document, targetPath);
                } catch (IOException e) {
                    throw new RuntimeException(
                        "Error during file processing", e
                    );
                } finally {
                    try {
                        document.close();
                    } catch (IOException e) {}
                }
            });
        return filePath;
    }

    public CompletableFuture<PDDocument> loadPdfAsync(Path filePath) {
        return CompletableFuture.supplyAsync(
            () -> loadFile(filePath)
        );
    }

    public PDDocument loadFile(Path filePath) {
        try {
            return Loader.loadPDF(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse file", e);
        }
    }

    public void processFile(PDDocument document, Path filePath) throws IOException {
        removeMetaData(document);
        removeJavaScript(document);
        removeEmbeddedFiles(document);
        removeAcroForm(document);
        // compression should be happening here by default 
        // could enable xref 
        document.save(filePath.toString());
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
