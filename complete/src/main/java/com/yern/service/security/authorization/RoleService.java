package com.yern.service.security.authorization;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Role;
import com.yern.repository.security.authorization.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
     * @param **Role** role - a role with or without RolePermissions set
     * 
     * **Rules for Standard Role Types** 
     * * OWNER: 
     *      * required permissions: 'OWN', 'READ', 'DELETE', and 'AUTHORIZE'
     *      * restricted permission: N/A
     * * EDITOR: 
     *      * required permissions: 'UPDATE' and 'READ'
     *      * restricted permission: 'OWN' and 'AUTHORIZE'
     * * READER: 
     *      * requires 'READ' roles 
     *      * restricts 'UPDATE', 'OWN', 'AUTHORIZE'
     * 
     * @return null if the role's permissions are valid 
     * 
     * @throws InvalidRoleException if role's permission aren't valid 
    */
    public void validateRolePermissions(Role role) {
        return;
    }
}