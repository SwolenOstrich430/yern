package com.yern.model.storage;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "file_access_controls")
@NoArgsConstructor
public class FileAccessControl {
    @Id 
    @Column 
    private Long userId;

    @Id 
    @Column 
    private Long roleId;

    @Id 
    @Column 
    private Long fileId;

    @Column 
    private LocalDateTime createdAt;

    @Column 
    private LocalDateTime updatedAt;

    @PrePersist 
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            updatedAt = createdAt;
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate 
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
