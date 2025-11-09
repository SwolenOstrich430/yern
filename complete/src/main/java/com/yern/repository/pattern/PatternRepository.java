package com.yern.repository.pattern;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yern.model.pattern.Pattern;


public interface PatternRepository extends JpaRepository<Pattern,Long> {}
