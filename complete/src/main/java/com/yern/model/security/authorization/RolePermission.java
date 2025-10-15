package com.yern.model.security.authorization;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles_permissions")
@Getter
@Setter 
@NoArgsConstructor
public class RolePermission {
    @Id 
    @ManyToOne
    private Role role;

    @Enumerated(EnumType.ORDINAL)
    private Permission permission;
}
