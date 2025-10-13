package com.yern.dto.pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SectionCreateRequest {
    private String name;
    private Long fileId;
    private Long patternId;
    private int sequence;
    private Long counterId;
}
