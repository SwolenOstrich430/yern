package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;

import org.springframework.http.MediaType;

import com.yern.exceptions.NotFoundException;
import com.yern.model.storage.FileImpl;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PDFProcessor implements FileProcessor {@Override
    public Path processFile(Path filePath) throws IOException, NotFoundException, NotUniqueException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processFile'");
    }

    @Override
    public boolean hasValidMediaType(MediaType mediaType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasValidMediaType'");
    }
}
