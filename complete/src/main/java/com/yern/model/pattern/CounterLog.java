package com.yern.model.pattern;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.yern.dto.messaging.MessagePayload;
import com.yern.dto.pattern.CounterLogCreateRequest;

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
@Table(name = "counter_logs")
@Getter
@Setter
@NoArgsConstructor
public class CounterLog implements MessagePayload {
    @Id 
    @GeneratedValue
    private UUID id;

    @Column 
    private int value;

    @Column 
    private Long counterId;

    @Column
    private String externalId;

    @Column
    private LocalDateTime createdAt;

    @PrePersist 
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static CounterLog from(CounterLogCreateRequest req) {
        CounterLog log = new CounterLog();
        Objects.requireNonNull(
            req.getCounterId(), 
            "Counter Id cannot be null."
        );         
        log.setCounterId(req.getCounterId());

        assert(req.getValue() >= 0);
        log.setValue(req.getValue());

        assert(!req.getExternalId().isEmpty());
        log.setExternalId(req.getExternalId());

        return log;
    }
}
