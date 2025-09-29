package com.yern.model.storage;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity
@Table(name="files")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileImpl {
    private Long id;
    private StorageProviderType storageProvider;
    private String rawPath;
    private String formattedPath;
    private String publicUrl;
    private String etag;
    private ProcessFileException error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean hasError() {
        return (
            getError() instanceof ProcessFileException
        );
    }

    public String getBasename() {
        return "";
    }
}
