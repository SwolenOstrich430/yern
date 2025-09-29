package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
        removeMetaData();
        removeSensitiveContent();
        flattenFile();
        compressFile();
    }

    public void removeMetaData() {

    }

    public void removeSensitiveContent() {

    }

    public void flattenFile() {

    }

    public void compressFile() {

    }

    @Override
    public boolean isValidMediaType(MediaType mediaType) {
        return this.mediaTypes.contains(mediaType);
    }
}
