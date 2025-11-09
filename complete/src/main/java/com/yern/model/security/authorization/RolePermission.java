package com.yern.model.security.authorization;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import jakarta.persistence.Column;
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
    public RolePermission(Long roleId, Permission permission) {
        this.roleId = roleId; 
        this.permission = permission;
    }

    @Id 
    @Column(name = "role_id")
    private Long roleId;

    @Id 
    @Column 
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Permission permission;
}
