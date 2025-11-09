package com.yern.service.storage.access;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.security.access.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.Role;
import com.yern.model.security.authorization.RoleType;
import com.yern.model.storage.FileAccessControl;
import com.yern.repository.storage.FileAccessControlRespository;
import com.yern.service.storage.file.access.FileAccessControlService;


public class FileAccessControlServiceTest {
    @Mock
    private FileAccessControlRespository accessRespository;
    @InjectMocks
    private FileAccessControlService service;
    @Spy
    @InjectMocks
    private FileAccessControlService spy;
    @Mock
    private Role role;
    @Mock
    private FileAccessControl accessRecord;

    private final Long userId = 1L;
    private final Long userId2 = 2L;
    private final Long fileId = 3L;
    private final Long roleId = 4L;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test 
    public void createOwner_callsGrantAccess() throws AccessDeniedException {
        doReturn(
            Optional.of(accessRecord)
        )
        .when(spy)
        .grantAccess(userId, userId, fileId, RoleType.OWNER);

        spy.createOwner(userId, fileId);
        
        verify(
            spy, 
            times(1)
        )
        .grantAccess(userId, userId, fileId, RoleType.OWNER);
    }

    @Test 
    public void verifyAccess_throwsAccessDeniedException_ifHasAccessReturnsFalse() {
        doReturn(false).when(spy).hasAccess(userId, fileId, Permission.OWN);
        
        assertThrows(
            AccessDeniedException.class,
            () -> spy.verifyAccess(userId, fileId, Permission.OWN)
        );
    }

    @Test 
    public void verifyAccess_doesNotThrow_ifHasAccessReturnsTrue() {
        doReturn(true).when(spy).hasAccess(userId, fileId, Permission.OWN);
        assertDoesNotThrow(
            () -> spy.verifyAccess(userId, fileId, Permission.OWN)
        );
    }

    @Test 
    public void hasAccess_returnsFalse_ifAMatchingFileAccessControlsRecordDoesNotExist() {
        doReturn(new ArrayList<>()) 
        .when(spy).findByUserIdAndFileId(userId, fileId);
        
        assertFalse(
            spy.hasAccess(userId, fileId, Permission.READ)
        );
    }
    
    @Test 
    public void verifyAcess_returnsFalse_ifCorrespondingFileAccessControlsHasMismatchOnPermissions() {
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(accessRecord);

        when(accessRecord.getRole()).thenReturn(role);
        when(accessRecord.getUserId()).thenReturn(userId);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.READ));
        doReturn(records).when(spy).findByUserIdAndFileId(userId, fileId);
        
        assertFalse(
            spy.hasAccess(userId, fileId, Permission.OWN)
        );
    }
    
    @Test 
    public void verifyAccess_returnsTrue_ifAccessFileControlsRecord_hasMatchingPermissions() {
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(accessRecord);

        when(accessRecord.getRole()).thenReturn(role);
        when(accessRecord.getUserId()).thenReturn(userId);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWN));
        doReturn(records).when(spy).findByUserIdAndFileId(userId, fileId);

        
        assertTrue(
            spy.hasAccess(userId, fileId, Permission.OWN)
        );
    }

    @Test 
    public void grantAccess_createsFileAccessControlEntry_ifRequestingUserHasAuthorizeAccess() throws AccessDeniedException {
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(mock(FileAccessControl.class));

        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            doReturn(records).when(spy).findByFileId(fileId);
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);
            when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWN));
            mockedStatic.when(() -> FileAccessControl.from(
                userId2,
                fileId,
                roleId
            )) 
            .thenReturn(accessRecord);

            spy.grantAccess(userId, userId2, fileId, role);
        }

        verify(
            spy,
            times(1)
        ).verifyAccess(
            userId,
            fileId,
            Permission.AUTHORIZE
        );

        verify(
            accessRespository,
            times(1)
        )
        .save(accessRecord);
    }

    @Test 
    public void grantAccess_createsFileAccessControlEntry_ifTheFileDoesNotHaveAnyEntriesInFileAccessControls() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWN);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            when(spy.findByFileId(fileId)).thenReturn(new ArrayList<>());
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);
            when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWN));

            mockedStatic.when(() -> FileAccessControl.from(
                userId,
                fileId,
                role.getId()
            )) 
            .thenReturn(accessRecord);

            spy.grantAccess(userId, userId, fileId, role);
        }

        verify(
            spy,
            times(0)
        ).verifyAccess(
            userId,
            fileId,
            Permission.OWN
        );

        verify(
            accessRespository,
            times(1)
        )
        .save(accessRecord);
    }

    @Test 
    public void grantAccess_doesNotCreateDuplicateEntry_ifMatchingFileAcessControlsEntryExists() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(FileAccessControl.from(userId2, fileId, roleId));
        
        when(role.getId()).thenReturn(roleId);
        when(spy.findByFileId(fileId)).thenReturn(records);

        assertEquals(records.get(0), FileAccessControl.from(userId2, fileId, roleId));
        FileAccessControl found = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found, records.get(0));

        verify(
            accessRespository,
            times(0)
        )
        .save(accessRecord);
    }

    @Test 
    public void grantAcess_returnsTheCreatedFileAccessControlsEntry() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(FileAccessControl.from(userId2, fileId, roleId));
        
        when(role.getId()).thenReturn(roleId);
        when(spy.findByFileId(fileId)).thenReturn(records);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWN));

        assertEquals(records.get(0), FileAccessControl.from(userId2, fileId, roleId));
        FileAccessControl found = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found, records.get(0));

        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWN);
            
        when(accessRespository.save(any(FileAccessControl.class))).thenReturn(accessRecord);
        FileAccessControl found2 = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found2, records.get(0));
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingUserId_doesNotHaveAnOWNERPermission() throws AccessDeniedException {
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWN));
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(mock(FileAccessControl.class));
        doReturn(records).when(spy).findByFileId(fileId);

        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.grantAccess(userId, userId2, fileId, role)
        );
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingAnyProvidedParamsDontExist() throws AccessDeniedException {
        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWN);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.verifyAccess(userId, fileId, Permission.OWN)
        );
    }

    @Test 
    public void grantAccess_throwsAccessDeniedError_ifInitialRoleForFileAccessDoesNotContainOWNER() throws AccessDeniedException {
        when(role.getRawPermissions()).thenReturn(new HashSet<>());
        doReturn(new ArrayList()).when(spy).findByFileId(fileId);
        doThrow(
            AccessDeniedException.class
        )
        .when(role)
        .validate(RoleType.OWNER, ResourceType.FILE);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.grantAccess(userId, userId, fileId, role)
        );
    }
}
