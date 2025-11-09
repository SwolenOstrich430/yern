package com.yern.config.pattern;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yern.mapper.pattern.PatternMapper;
import com.yern.mapper.pattern.SectionMapper;

@Configuration
public class PatternConfig {

    @Bean
    public SectionMapper sectionMapper() {
        return new SectionMapper();
    }

    @Bean 
    public PatternMapper patternMapper() {
        return new PatternMapper();
    }
}
