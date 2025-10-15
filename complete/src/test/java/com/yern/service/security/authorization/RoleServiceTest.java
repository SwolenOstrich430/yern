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

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleIsNull() {
        assertThrows(
            AssertionError.class, 
            () -> service.validateRolePermissions(null)
        );
    }

     @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleDoesNotHavePermissions() {
        when(role.getRawPermissions()).thenReturn(null);

        assertThrows(
            AssertionError.class, 
            () -> service.validateRolePermissions(role)
        );

        when(role.getRawPermissions()).thenReturn(new HashSet<>());
        
        assertThrows(
            AssertionError.class, 
            () -> service.validateRolePermissions(role)
        );
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsOwnerAndDoesNotHaveRequiredPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE,
            Permission.READ,
            Permission.DELETE,
            Permission.AUTHORIZE,
            Permission.OWN
        );
        HashSet<Permission> copiedPermissions = new HashSet<>();
        copiedPermissions.addAll(permissions);

        for (Permission permission : permissions) {
            copiedPermissions.remove(permission);
            when(role.getType()).thenReturn(RoleType.OWNER);
            when(role.getRawPermissions()).thenReturn(copiedPermissions);
        
            assertThrows(
                AssertionError.class, 
                () -> service.validateRolePermissions(role)
            );

            copiedPermissions.add(permission);
        }
    }

    @Test 
    public void validateRolePermissions_doesNotThrowsAssertionError_ifRoleTypeIsOwnerAndHasRequiredPermission() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE,
            Permission.READ,
            Permission.DELETE,
            Permission.AUTHORIZE,
            Permission.OWN
        );

        when(role.getType()).thenReturn(RoleType.OWNER);
        when(role.getRawPermissions()).thenReturn(permissions);
    
        assertDoesNotThrow(
            () -> service.validateRolePermissions(role)
        );
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsEditorAndDoesNotHaveRequiredPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE,
            Permission.READ
        );
        HashSet<Permission> copiedPermissions = new HashSet<>();
        copiedPermissions.addAll(permissions);

        for (Permission permission : permissions) {
            copiedPermissions.remove(permission);
            when(role.getType()).thenReturn(RoleType.OWNER);
            when(role.getRawPermissions()).thenReturn(copiedPermissions);
        
            assertThrows(
                AssertionError.class, 
                () -> service.validateRolePermissions(role)
            );

            copiedPermissions.add(permission);
        }
    }

    @Test 
    public void validateRolePermissions_doesNotThrowAssertionError_ifRoleTypeIsEditorAndHasRequiredPermission() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE,
            Permission.READ
        );

        when(role.getType()).thenReturn(RoleType.EDITOR);
        when(role.getRawPermissions()).thenReturn(permissions);
    
        assertDoesNotThrow(
            () -> service.validateRolePermissions(role)
        );
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsReaderAndDoesNotHaveRequiredPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.OWN
        );
        
        when(role.getType()).thenReturn(RoleType.READER);
        when(role.getRawPermissions()).thenReturn(permissions);
    
        assertThrows(
            AssertionError.class, 
            () -> service.validateRolePermissions(role)
        );
    }

    @Test 
    public void validateRolePermissions_doesNotThrowAssertionError_ifRoleTypeIsReaderAndHasRequiredPermission() {
        Set<Permission> permissions = Set.of(
            Permission.READ
        );

        when(role.getType()).thenReturn(RoleType.READER);
        when(role.getRawPermissions()).thenReturn(permissions);
    
        assertDoesNotThrow(
            () -> service.validateRolePermissions(role)
        );
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsNotOwnerButHasOwnerSpecificPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.OWN,
            Permission.DELETE,
            Permission.AUTHORIZE
        );

        for (Permission permission : permissions) {
            when(role.getType()).thenReturn(RoleType.READER);
            when(role.getRawPermissions()).thenReturn(Set.of(permission));
        
            assertThrows(
                AssertionError.class, 
                () -> service.validateRolePermissions(role)
            );

            when(role.getType()).thenReturn(RoleType.EDITOR);
            when(role.getRawPermissions()).thenReturn(Set.of(permission));
        
            assertThrows(
                AssertionError.class, 
                () -> service.validateRolePermissions(role)
            );
        }
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsNotEditorButHasEditorSpecificPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE
        );

        for (Permission permission : permissions) {
            when(role.getType()).thenReturn(RoleType.READER);
            when(role.getRawPermissions()).thenReturn(Set.of(permission));
        
            assertThrows(
                AssertionError.class, 
                () -> service.validateRolePermissions(role)
            );
        }
    }
}
