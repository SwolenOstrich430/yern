package com.yern.model.common;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AuditTimestamp {
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, updatable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
    private LocalDateTime updatedAt;

    public AuditTimestamp() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
}
