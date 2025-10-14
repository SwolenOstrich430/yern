package com.yern.service.storage.file.processing;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;
import com.yern.service.storage.NotUniqueException;

public interface FileProcessorOrchestrator {
    Path processFile(Path filePath) throws IOException, NotFoundException, NotUniqueException;
    FileProcessor getProvider(Path filePath) throws NotUniqueException, IOException, NotFoundException;
    boolean hasValidMediaType(Path filePath) throws IOException;
    boolean isValidMediaType(MediaType mediaType);
}

