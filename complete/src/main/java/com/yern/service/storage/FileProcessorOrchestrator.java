package com.yern.service.storage;

import com.yern.model.storage.FileImpl;

public interface FileProcessorOrchestrator extends FileProcessor {
    public FileProcessor getProvider(FileImpl file);
}

