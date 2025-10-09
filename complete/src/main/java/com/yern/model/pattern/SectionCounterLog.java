package com.yern.model.pattern;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.cglib.core.Local;

import com.yern.dto.pattern.SectionCounterLogCreateRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "section_counter_logs")
@Getter
@Setter
@NoArgsConstructor
public class SectionCounterLog {
    @Id 
    @GeneratedValue
    private UUID id;

    @Column 
    private int value;

    @Column
    private Long sectionId;

    @Column
    private LocalDateTime createdAt;

    @PrePersist 
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static SectionCounterLog from(SectionCounterLogCreateRequest req) {
        SectionCounterLog log = new SectionCounterLog();
        log.setSectionId(req.getSectionId());
        log.setValue(req.getValue());

        return log;
    }
}
