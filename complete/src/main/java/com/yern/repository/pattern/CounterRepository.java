package com.yern.repository.pattern;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yern.model.pattern.Counter;

public interface CounterRepository extends JpaRepository<Counter, Long> {}
