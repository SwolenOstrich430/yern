package com.yern.model.security;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter 
@NoArgsConstructor
public class Role {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column 
    private String name; 

    @Column 
    private String identifier; 
    
    @OneToMany(
        mappedBy = "role", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true
    )
    private Set<RolePermission> permissions;

    // TODO: add unit testing 
    public Set<Permission> getRawPermissions() {
        return permissions
                .stream()
                .map(permission -> permission.getPermission())
                .collect(Collectors.toSet());
    }
}
