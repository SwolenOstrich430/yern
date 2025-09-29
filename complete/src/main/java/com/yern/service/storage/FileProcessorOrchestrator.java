package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;

public interface FileProcessorOrchestrator extends FileProcessor {
    FileProcessor getProvider(Path filePath) throws NotUniqueException, IOException, NotFoundException;
    public boolean hasValidMediaType(MediaType mediaType);
}

