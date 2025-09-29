package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;


public interface FileProcessor {
    public void processFile(Path filePath) throws IOException, NotFoundException, NotUniqueException;
    public boolean isValidMediaType(MediaType mediaType);
}
