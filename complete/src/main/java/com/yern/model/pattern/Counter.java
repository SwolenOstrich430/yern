package com.yern.model.pattern;

import java.time.LocalDateTime;

import com.yern.model.common.AuditTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "counters")
@Getter
@Setter
@NoArgsConstructor
public class Counter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column 
    private Long value;

    @Column 
    private AuditTimestamp auditTimestamps;

    @Column 
    private LocalDateTime lastResetAt;
}
