package com.yern.service.security.authorization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.Role;
import com.yern.model.security.authorization.RoleType;
import com.yern.repository.security.authorization.RoleRepository;

import io.jsonwebtoken.lang.Assert;

public class RoleServiceTest {
    @Mock 
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService service; 
    
    @Mock 
    private ResourceType resourceType;

    @Mock 
    private RoleType roleType;

    @Mock 
    private Role role;

    private final Long roleId = 1L;

    @BeforeEach 
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test 
    public void getRoleById_returnsResult_ofRoleRepsotiroy_getRoleById() {
        when(
            roleRepository.getRoleById(roleId)
        )
        .thenReturn(Optional.of(role));
        
        Optional<Role> foundRole = service.getRoleById(roleId);
        assertTrue(foundRole.isPresent());
        assertEquals(foundRole.get(), role);
        
        verify(
            roleRepository, 
            times(1)
        )
        .getRoleById(roleId);
    }

    @Test 
    public void findRolesByResource_callsRoleRepository_findByResource() {
        List<Role> roles = new ArrayList<>();
        roles.add(role);

        when(roleRepository.findByResource(resourceType)).thenReturn(roles);
        
        List<Role> foundRoles = service.findRolesByResource(resourceType);
        
        assertEquals(roles, foundRoles);
        
        verify(
            roleRepository, 
            times(1)
        )
        .findByResource(resourceType);
    }

    @Test 
    public void getRoleByResourceAndType_callsRoleRepository_getRoleByResourceAndType() {
        when(
            roleRepository.getRoleByResourceAndType(
                resourceType,
                roleType
            )
        )
        .thenReturn(Optional.of(role));
        
        Optional<Role> foundRole = service.getRoleByResourceAndType(
            resourceType, roleType
        );
                
        verify(
            roleRepository, 
            times(1)
        )
        .getRoleByResourceAndType(resourceType, roleType);
    }
}
