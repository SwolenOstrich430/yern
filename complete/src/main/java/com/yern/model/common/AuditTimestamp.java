package com.yern.model.common;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditTimestamp implements Serializable {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
