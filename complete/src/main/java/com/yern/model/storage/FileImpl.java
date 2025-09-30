package com.yern.model.storage;

import java.io.IOError;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Entity(name="files")
@Table(name="files")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileImpl implements Serializable {
    @Id 
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private StorageProviderType storageProvider;
    @Column 
    private String rawPath;
    @Column 
    private String formattedPath;
    @Column 
    private String publicUrl;
    @Column 
    private String etag;
    @Column 
    private Throwable error;
    @Column 
    private LocalDateTime createdAt;
    @Column 
    private LocalDateTime updatedAt;

    public boolean hasError() {
        return (
            getError() instanceof IOException
        );
    }

    public String getBasename() {
        return "";
    }
}
