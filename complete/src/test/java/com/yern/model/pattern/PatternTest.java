package com.yern.model.pattern;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class PatternTest {
    @Test 
    public void noArgsConstructor() {
        assertInstanceOf(Pattern.class, new Pattern());
    }

    @Test 
    public void allArgsConstructor() {
        assertInstanceOf(
            Pattern.class, 
            new Pattern(
                Long.valueOf(1), 
                "name", 
                "description", 
                LocalDateTime.now(), 
                LocalDateTime.now()
            )
        );

    }
}
