package com.yern.repository.pattern;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yern.model.pattern.UserPattern;

public interface UserPatternRepository extends JpaRepository<UserPattern, Long> {
    public Optional<UserPattern> findByUserIdAndPatternId(Long userId, Long patternId);
}
