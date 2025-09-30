package com.yern.service.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;

import com.yern.exceptions.NotFoundException;

@Component 
public class GenericFileProcessor implements FileProcessorOrchestrator {
    private List<FileProcessor> processors;
    
    public GenericFileProcessor(
        @Autowired List<FileProcessor> processors
    ) {
        this.processors = processors;
    }

    public void processFile(
        Path filePath
    ) throws IOException {
        FileProcessor processor = getProvider(filePath);
        processor.processFile(filePath);
    }

    public boolean hasValidMediaType(Path filePath) throws IOException {
        return isValidMediaType(
            getMediaType(filePath)
        );
    }

    public boolean isValidMediaType(MediaType mediaType) {
        return processors
                .stream()
                .anyMatch(prov -> prov.isValidMediaType(mediaType));
    }

    public FileProcessor getProvider(
        Path filePath
    ) throws IOException {
        MediaType mediaType = getMediaType(filePath);

        List<FileProcessor> processor = 
                    processors
                        .stream()
                        .filter(prov -> prov.isValidMediaType(mediaType))
                        .collect(Collectors.toList());

        if (processor.isEmpty()) {
            throw new NotFoundException("");
        }

        if (processor.size() > 1) {
            throw new NotUniqueException(processor);
        }

        return processor.get(0);
    }

    // TODO: move this into a util class 
    public MediaType getMediaType(Path filePath) throws IOException {
        Optional<MediaType> mimeTypeOptional = MediaTypeFactory.getMediaType(filePath.toString());
        
        mimeTypeOptional.orElseThrow(
            () -> new IOException("Invalid media type for: " + filePath.toString())
        );

        return mimeTypeOptional.get();
    }
}
