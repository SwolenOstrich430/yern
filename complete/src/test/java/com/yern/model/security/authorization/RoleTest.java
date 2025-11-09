package com.yern.model.security.authorization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RoleTest {

    private Role role;
    private Role spy;

    @BeforeEach
    public void setup() {
        role = new Role();
        spy = spy(role);
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleIsNull() {
        assertThrows(
            AssertionError.class, 
            () -> role.validatePermissions(null)
        );
    }

     @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleDoesNotHavePermissions() {
        doReturn(null).when(spy).getRawPermissions();
        assertThrows(
            AssertionError.class, 
            () -> spy.validatePermissions()
        );

        doReturn(new HashSet<>()).when(spy).getRawPermissions();
        
        assertThrows(
            AssertionError.class, 
            () -> spy.validatePermissions()
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

            doReturn(RoleType.OWNER).when(spy).getType();
            doReturn(copiedPermissions).when(spy).getRawPermissions();
        
            assertThrows(
                AssertionError.class, 
                () -> spy.validatePermissions()
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

        doReturn(RoleType.OWNER).when(spy).getType();
        doReturn(permissions).when(spy).getRawPermissions();
    
        assertDoesNotThrow(
            () -> spy.validatePermissions()
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
            doReturn(RoleType.EDITOR).when(spy).getType();
            doReturn(copiedPermissions).when(spy).getRawPermissions();
        
            assertThrows(
                AssertionError.class, 
                () -> spy.validatePermissions()
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

        doReturn(RoleType.EDITOR).when(spy).getType();
        doReturn(permissions).when(spy).getRawPermissions();
    
        assertDoesNotThrow(
            () -> spy.validatePermissions()
        );
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsReaderAndDoesNotHaveRequiredPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.OWN
        );
        
        doReturn(RoleType.READER).when(spy).getType();
        doReturn(permissions).when(spy).getRawPermissions();
    
        assertThrows(
            AssertionError.class, 
            () -> spy.validatePermissions()
        );
    }

    @Test 
    public void validateRolePermissions_doesNotThrowAssertionError_ifRoleTypeIsReaderAndHasRequiredPermission() {
        Set<Permission> permissions = Set.of(
            Permission.READ
        );

        doReturn(RoleType.READER).when(spy).getType();
        doReturn(permissions).when(spy).getRawPermissions();
    
        assertDoesNotThrow(
            () -> spy.validatePermissions()
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
            doReturn(RoleType.READER).when(spy).getType();
            doReturn(Set.of(permission)).when(spy).getRawPermissions();
    
            assertThrows(
                AssertionError.class, 
                () -> spy.validatePermissions()
            );

            doReturn(RoleType.EDITOR).when(spy).getType();
            doReturn(Set.of(permission)).when(spy).getRawPermissions();
    
            assertThrows(
                AssertionError.class, 
                () -> spy.validatePermissions()
            );
        }
    }

    @Test 
    public void validateRolePermissions_throwsAssertionError_ifRoleTypeIsNotEditorButHasEditorSpecificPermissions() {
        Set<Permission> permissions = Set.of(
            Permission.UPDATE
        );

        for (Permission permission : permissions) {
            doReturn(RoleType.READER).when(spy).getType();
            doReturn(Set.of(permission)).when(spy).getRawPermissions();
        
            assertThrows(
                AssertionError.class, 
                () -> spy.validatePermissions()
            );
        }
    }
}
