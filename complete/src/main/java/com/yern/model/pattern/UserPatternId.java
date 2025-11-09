package com.yern.model.pattern;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class UserPatternId implements Serializable {
    private Long userId;
    private Long patternId;
}