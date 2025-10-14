package com.yern.model.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDAnnotationAdditionalActions;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.yern.service.storage.file.processing.PDFProcessor;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class PDFProcessorIntegrationTest {
    private PDDocument document;
    private PDDocument cleanedDocument;
    private File embeddedFile;
    private final PDFProcessor processor = new PDFProcessor();
    private final String documentName = UUID.randomUUID().toString() + ".pdf";
    private final String embeddedFileName = UUID.randomUUID().toString() + ".txt";
    /**
     * 1. create a pdf document with pdf box 
     * 2. add a document id 
     * 3. add catalog metadata 
     * 4. add PDActionJavaScript to getDocumentCatalog().getOpenAction()
     * 5. add widgets with actions that include COSName.JS
     * 6. add embedded files 
     * 7. set an AcroForm
     */
    @BeforeAll
    private void setup() throws IOException {
        document = new PDDocument();
        document.setDocumentId(1L);
        PDPage page = new PDPage();
        document.addPage(page); 


        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDMetadata metadata = document.getDocumentCatalog().getMetadata();
        if (metadata == null) {
            metadata = new PDMetadata(document);
            catalog.setMetadata(metadata);
        }

        // Get the document information object
        PDDocumentInformation info = document.getDocumentInformation();
        // Set standard metadata fields
        info.setTitle("My PDF Title");
        info.setAuthor("John Doe");
        info.setSubject("Example PDFBox Metadata");
        info.setKeywords("PDF, Metadata, PDFBox, Java");
        info.setCreator("My Java Application");
        info.setProducer("Apache PDFBox");
        info.setCreationDate(Calendar.getInstance());
        info.setModificationDate(Calendar.getInstance());
        // Set custom metadata fields
        info.setCustomMetadataValue("ProjectName", "PDFBox Demo");
        info.setCustomMetadataValue("Version", "1.0");

        String javaScriptCode = "app.alert('Hello from PDFBox JavaScript!');";
        PDActionJavaScript jsAction = new PDActionJavaScript(javaScriptCode);
        document.getDocumentCatalog().setOpenAction(jsAction);

        PDAnnotationLink link = new PDAnnotationLink();
        link.setAction(jsAction);
        
        PDAnnotationWidget buttonWidget = new PDAnnotationWidget();
        PDAcroForm acroForm = new PDAcroForm(document);
        document.getDocumentCatalog().setAcroForm(acroForm);
        PDPushButton pushButton = new PDPushButton(acroForm);
        pushButton.setPartialName("push");
        acroForm.getFields().add(pushButton);
        
        PDAnnotationWidget widget = pushButton.getWidgets().get(0);
        page.getAnnotations().add(widget);
        widget.setRectangle(new PDRectangle(50, 500, 100, 100)); // position on the page
        widget.setPrinted(true);
        widget.setPage(page);

        PDActionJavaScript javascriptAction = new PDActionJavaScript("app.alert(\"button pressed\")");
        PDAnnotationAdditionalActions actions = new PDAnnotationAdditionalActions();
        actions.setU(javascriptAction);
        widget.setActions(actions);

        embeddedFile = new File(embeddedFileName);
        embeddedFile.createNewFile();
        FileInputStream fileInputStream = new FileInputStream(embeddedFile);
        PDEmbeddedFile embeddedFileObj = new PDEmbeddedFile(document, fileInputStream);
        embeddedFileObj.setSubtype("text/plain"); 
        embeddedFileObj.setCreationDate(Calendar.getInstance());
        PDComplexFileSpecification fileSpecification = new PDComplexFileSpecification();
        fileSpecification.setFile("artificial text.pdf");
        fileSpecification.setEmbeddedFile(embeddedFileObj);

        Map<String, PDComplexFileSpecification> embeddedFileMap = new HashMap<String, PDComplexFileSpecification>();
        embeddedFileMap.put("artificial text.pdf", fileSpecification);

        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();
        efTree.setNames(embeddedFileMap);

        PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
        names.setEmbeddedFiles(efTree);
        document.getDocumentCatalog().setNames(names);

        document.save(documentName);

        assertDoesNotThrow(
            () -> processor.processFile(Path.of(documentName))
        );

        cleanedDocument = Loader.loadPDF(new File(documentName));
    }

    @AfterAll 
    public void tearDown() {
        try {
            document.close();
            cleanedDocument.close();
        } catch (IOException e) {}

        if (embeddedFile != null && embeddedFile.exists()) {
            embeddedFile.delete();
        }

        File origFile = new File(documentName);
        if (origFile.exists()) {
            origFile.delete();
        }
    }

    @Test 
    @Order(1)
    // TODO: make sure that all custom shit is removed
    public void processFile_removesMetaData() {
        assertEquals(
            document.getDocument().getDocumentID(),
            document.getDocument().getDocumentID()
        );
        assertNotNull(document.getDocumentCatalog().getMetadata());

        assertNotEquals(
            cleanedDocument.getDocument().getDocumentID(),
            document.getDocument().getDocumentID()
        );
        assertNull(cleanedDocument.getDocumentCatalog().getMetadata());
    }

    @Test 
    @Order(2)
    public void processFile_removesJavaScript() throws IOException {
        assertNotNull(document.getDocumentCatalog().getOpenAction());
        assertNull(cleanedDocument.getDocumentCatalog().getOpenAction());

        List<PDAnnotation> oldAnnotations = new ArrayList<>();
        for (PDPage page : document.getPages()) {;
            if (page.getAnnotations() != null) {
                oldAnnotations.addAll(page.getAnnotations());
            }
        }
        assertFalse(oldAnnotations.isEmpty());

        List<PDAnnotation> annotations = new ArrayList<>();
        for (PDPage page : cleanedDocument.getPages()) {;
            if (page.getAnnotations() != null) {
                annotations.addAll(page.getAnnotations());
            }
        }
        assertTrue(annotations.isEmpty());
    }

    @Test 
    @Order(3)
    public void processFile_removesEmbeddedFiles() {
        assertNotNull(
            document.getDocumentCatalog().getNames()
        );
        assertNotNull(
            document.getDocumentCatalog().getNames().getEmbeddedFiles()
        );

        assertNotNull(
            cleanedDocument.getDocumentCatalog().getNames()
        );
        assertNull(
            cleanedDocument.getDocumentCatalog().getNames().getEmbeddedFiles()
        );
    }

    @Test 
    @Order(4)
    public void processFile_removeAcroForm() {
        assertNotNull(document.getDocumentCatalog().getAcroForm());
        assertNull(cleanedDocument.getDocumentCatalog().getAcroForm());
    }
}
