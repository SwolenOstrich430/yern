package com.yern.model.security.authorization;

import java.util.Set;
import java.util.stream.Collectors;

import com.yern.model.security.ResourceType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: migrations and make sure that permissions are validated 
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
    @Enumerated
    private ResourceType resource;

    @Column 
    @Enumerated(EnumType.STRING)
    private RoleType type; 

    @OneToMany(
        mappedBy = "role", 
        cascade = CascadeType.ALL, 
        orphanRemoval = true,
        fetch = FetchType.EAGER
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
