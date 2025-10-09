package com.yern.model.pattern;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cglib.core.Local;

import com.yern.model.user.User;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="patterns")
@Getter 
@Setter 
@AllArgsConstructor
@NoArgsConstructor
public class Pattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Basic
    @Column
    private String name;
    
    @Basic
    @Column
    private String description;

    @OneToMany(
        mappedBy = "patternId"
    )
    private Set<UserPattern> userPatterns;

    @Basic
    @Column
    private LocalDateTime createdAt;
    
    @Basic
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void preInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
}
