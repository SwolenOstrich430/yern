package com.yern.repository.pattern;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yern.model.pattern.Section;

public interface SectionRepository extends JpaRepository<Section,Long> {
    Section getById(Long sectionIdLong);
} 
