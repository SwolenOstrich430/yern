package com.yern.dto.pattern;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SectionCounterLogCreateRequest {
    private int value; 
    @JsonAlias("section_id")
    private Long sectionId;
}
