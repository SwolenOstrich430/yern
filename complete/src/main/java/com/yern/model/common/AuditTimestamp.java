package com.yern.model.common;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AuditTimestamp {
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    public AuditTimestamp() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
}
