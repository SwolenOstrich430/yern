package com.yern.service.security.authorization;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.Role;
import com.yern.model.security.authorization.RoleType;
import com.yern.repository.security.authorization.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    private RoleRepository roleRepository;

    public RoleService(
        @Autowired RoleRepository roleRepository
    ) {
        this.roleRepository = roleRepository;
    }

    /**
     * @param Long roleId - Id of the Role you're looking for. 
     * 
     * @return Optional<Role> 
    */ 
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.getRoleById(roleId);
    }
     
    /** 
     * @param **ResourceType** resourceType - a valid resource that's being requested
     * 
     * @return **List<Role>** matching the provided resource.
     * 
     * @throws InvalidInputException if resourceType is null or not a registered type. 
    */
    public List<Role> findRolesByResource(ResourceType resource) {
        return roleRepository.findByResource(resource);
    }

    /**
     * TODO: move this to Role?
     * 
     * @param **Role** role - a role with or without RolePermissions set
     * 
     * **Rules for Standard Role Types** 
     * * OWNER: 
     *      * required permissions: 'OWN', 'UPDATE', 'READ', 'DELETE', and 'AUTHORIZE'
     *      * restricted permission: N/A
     * * EDITOR: 
     *      * required permissions: 'UPDATE' and 'READ'
     *      * restricted permission: 'OWN' and 'AUTHORIZE'
     * * READER: 
     *      * requires 'READ' roles 
     *      * restricts 'UPDATE', 'OWN', 'AUTHORIZE'
     * 
     * All of the baove should be enforced in the database. Adding this 
     * here as an extra validtor since this should be enabled before 
     * we can expect the app to actually be able to run. 
     * 
     * @return null if the role's permissions are valid 
     * 
     * @throws AssertionError if role's permission aren't valid
     * @throws AssertionError if role is null or its permissions are empty
    */
    public void validateRolePermissions(Role role) {
        assert(role != null);
        Set<Permission> permissions = role.getRawPermissions();
        assert(permissions != null);
        assert(!permissions.isEmpty());

        RoleType type = role.getType();

        if (type == RoleType.OWNER) {
            assert(permissions.contains(Permission.OWN));
            assert(permissions.contains(Permission.AUTHORIZE));
            assert(permissions.contains(Permission.DELETE));
        } else {
            assert(!permissions.contains(Permission.OWN));
            assert(!permissions.contains(Permission.DELETE));
            assert(!permissions.contains(Permission.AUTHORIZE));
        }

        if (type == RoleType.EDITOR || type == RoleType.OWNER) {
            assert(permissions.contains(Permission.UPDATE));
            assert(permissions.contains(Permission.READ));
        } else {
            assert(!permissions.contains(Permission.UPDATE));
        }

        if (type == RoleType.READER) {
            assert(permissions.contains(Permission.READ));
        }
    }
}