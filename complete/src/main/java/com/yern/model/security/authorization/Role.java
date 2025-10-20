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

    public Role(
        Long id,
        String name,
        RoleType type,
        Set<RolePermission> permissions
    ) {
        setId(id);
        setName(name);
        setType(type);
        setPermissions(permissions);
    }

    public void setRawPermissions(Set<Permission> permissions) {
        validatePermissions(permissions);
        
        this.permissions = 
            permissions
                .stream()
                .map(permission -> new RolePermission(this, permission))
                .collect(Collectors.toSet());
    }

    public void setPermissions(Set<RolePermission> permissions) {
        Set<Permission> rawPermissions = getRawPermissions(permissions);
        setRawPermissions(rawPermissions);
    }

    public boolean isType(RoleType type) {
        assert(type != null);
        return this.type == type;
    }

    public boolean controlsResource(ResourceType resource) {
        assert(resource != null);
        return this.resource == resource;
    }

    public void validate(RoleType type, ResourceType resource) {
        assert(isType(type));
        assert(controlsResource(resource));
        validatePermissions();
    }

    public void validatePermissions() {
        validatePermissionsRaw(
            getRawPermissions(),
            getType()
        );
    }

    public void validatePermissions(Set<Permission> permission) {
        validatePermissionsRaw(permission, type);
    }
    
    private void validatePermissionsRaw(Set<Permission> permissions, RoleType roleType) {
        assert(permissions != null);
        assert(!permissions.isEmpty());
        assert(roleType != null);

        if (roleType == RoleType.OWNER) {
            assert(permissions.contains(Permission.OWN));
            assert(permissions.contains(Permission.AUTHORIZE));
            assert(permissions.contains(Permission.DELETE));
        } else {
            assert(!permissions.contains(Permission.OWN));
            assert(!permissions.contains(Permission.DELETE));
            assert(!permissions.contains(Permission.AUTHORIZE));
        }

        if (roleType == RoleType.EDITOR || roleType == RoleType.OWNER) {
            assert(permissions.contains(Permission.UPDATE));
            assert(permissions.contains(Permission.READ));
        } else {
            assert(!permissions.contains(Permission.UPDATE));
        }

        if (roleType == RoleType.READER) {
            assert(permissions.contains(Permission.READ));
        }
    }

    // TODO: add unit testing 
    public Set<Permission> getRawPermissions() {
        return getRawPermissions(permissions);
    }

    private Set<Permission> getRawPermissions(Set<RolePermission> permissions) {
        return permissions
                .stream()
                .map(permission -> permission.getPermission())
                .collect(Collectors.toSet());
    }
}
