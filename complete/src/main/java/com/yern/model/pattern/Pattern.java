package com.yern.model.pattern;

import java.time.LocalDateTime;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="users")
@Getter 
@Setter 
@AllArgsConstructor
@NoArgsConstructor
public class Pattern {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Basic
    @Column
    private String name;
    
    @Basic
    @Column
    private String description;
    
    @Basic
    @Column
    private LocalDateTime created_at;
    
    @Basic
    @Column
    private LocalDateTime updated_at;

    
}
