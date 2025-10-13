package com.yern.model.pattern;

import java.time.LocalDateTime;

import org.hibernate.event.spi.LockEventListener;
import org.springframework.cglib.core.Local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
    private LocalDateTime createdAt;

    @Column 
    private LocalDateTime updatedAt;

    @Column 
    private LocalDateTime lastResetAt;

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
}
