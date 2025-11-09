package com.yern.dto.pattern;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SectionCreateResponse {
    private Long id;
    private String name;
    private Long fileId;
    private Long counterId;
    private String fileUrl;
    private Long patternId;
    private int sequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
