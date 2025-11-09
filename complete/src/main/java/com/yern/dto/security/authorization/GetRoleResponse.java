package com.yern.dto.security.authorization;

import java.util.Set;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.Role;
import com.yern.model.security.authorization.RoleType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class GetRoleResponse {
    private Long id;
    private String displayName;
    private ResourceType resource;
    private RoleType type;
    private Set<Permission> permissions;

    public static GetRoleResponse from(Role role) {
        GetRoleResponse resp = new GetRoleResponse();
        
        resp.setId(role.getId());
        resp.setDisplayName(role.getDisplayName());
        resp.setResource(role.getResource());
        resp.setType(role.getType());
        resp.setPermissions(role.getRawPermissions());

        return resp;
    }
}
