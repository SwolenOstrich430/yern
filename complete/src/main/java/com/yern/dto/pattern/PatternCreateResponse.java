package com.yern.dto.pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class PatternCreateResponse {
    private Long id;
    private String name;
    private String description;
}
