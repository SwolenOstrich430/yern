package com.yern.service.security.authorization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Role;
import com.yern.repository.security.authorization.RoleRepository;

public class RoleServiceTest {
    @Mock 
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService service; 
    
    @Mock 
    private ResourceType resourceType;

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
    public void getRoleById_callsRoleRepository_findByResource() {
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
}
