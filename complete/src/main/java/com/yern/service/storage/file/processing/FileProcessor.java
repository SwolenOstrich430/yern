package com.yern.service.storage.file.processing;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;
import com.yern.service.storage.NotUniqueException;


public interface FileProcessor {
    public Path processFile(Path filePath, Path targetPath) throws IOException, NotFoundException, NotUniqueException;
    public boolean isValidMediaType(MediaType mediaType);
}
