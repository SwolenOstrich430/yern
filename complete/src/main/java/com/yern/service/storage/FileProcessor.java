package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;


public interface FileProcessor {
    public Path processFile(Path filePath) throws IOException, NotFoundException, NotUniqueException;
    public boolean hasValidMediaType(MediaType mediaType);
}
