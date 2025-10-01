package com.yern.service.storage.file;

import java.io.IOException;
import java.nio.file.Path;

import com.yern.exceptions.NotFoundException;
import com.yern.service.storage.NotUniqueException;

public interface FileProcessorOrchestrator extends FileProcessor {
    FileProcessor getProvider(Path filePath) throws NotUniqueException, IOException, NotFoundException;
    public boolean hasValidMediaType(Path filePath) throws IOException;
}

