package com.yern.model.pattern;


import java.time.LocalDateTime;

import com.yern.model.storage.FileImpl;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="sections")
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patternId;
    private String name;
    private String notes;
    private FileImpl file;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
