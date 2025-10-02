package com.yern.model.pattern;


import java.time.LocalDateTime;

import com.yern.model.storage.FileImpl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name="sections")
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter
public class Section {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column 
    private Long patternId;
    @Column
    private String name;
    @Column
    private String notes;
    @JoinColumn
    @ManyToOne
    private FileImpl file;
    @Column
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;
}
