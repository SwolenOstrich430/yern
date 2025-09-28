package com.yern.model.pattern;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
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
    private Long id;
    private String name;
    private String description;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    
}
