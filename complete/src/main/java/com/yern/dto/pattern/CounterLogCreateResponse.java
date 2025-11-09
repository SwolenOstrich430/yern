package com.yern.dto.pattern;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class CounterLogCreateResponse {
    private UUID id; 
    private int value;
    private Long counterId;
    private LocalDateTime createdAt;
    private Throwable error;
}
