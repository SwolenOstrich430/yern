package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import com.yern.model.storage.FileImpl;

public interface FileProcessor {
    public Path processFile(FileImpl file, Path filePath);
    public boolean isValidFileType(Path filePath);
    public void validateFileType(Path filePath) throws IOException;
}
