package com.yern.model.pattern;

import java.time.LocalDateTime;
import java.util.Set;

import com.yern.model.common.AuditTimestamp;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="patterns")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class Pattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Basic
    @Column
    private String name;
    
    @Basic
    @Column
    private String description;

    @OneToMany(
        mappedBy = "patternId"
    )
    private Set<UserPattern> userPatterns;

    @Basic
    @Column
    private AuditTimestamp auditTimestamps;

    public Pattern(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.auditTimestamps = new AuditTimestamp(
            createdAt, updatedAt
        );
    }

    @PrePersist
    public void preInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
            updatedAt = createdAt;
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
