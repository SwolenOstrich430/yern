package com.yern.service.storage.access;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.yern.model.security.Permission;
import com.yern.model.security.Role;
import com.yern.model.storage.FileAccessControl;
import com.yern.repository.storage.FileAccessControlRespository;
import com.yern.service.storage.file.access.FileAccessControlService;
import com.yern.service.user.UserService;

public class FileAccessControlServiceTest {
    @Mock
    private UserService userService;
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
    public void verifyAccess_throwsAccessDeniedException_ifHasAccessReturnsFalse() {
        doReturn(false).when(spy).hasAccess(userId, fileId, Permission.OWNER);
        assertThrows(
            AccessDeniedException.class,
            () -> spy.verifyAccess(userId, fileId, Permission.OWNER)
        );

        ArrayList<FileAccessControl> records = mock(ArrayList.class);
        doReturn(false).when(spy).hasAccess(userId, records, Permission.OWNER);
        
        assertThrows(
            AccessDeniedException.class,
            () -> spy.verifyAccess(userId, records, Permission.OWNER)
        );
    }

    @Test 
    public void verifyAccess_doesNotThrow_ifHasAccessReturnsTrue() {
        doReturn(true).when(spy).hasAccess(userId, fileId, Permission.OWNER);
        assertDoesNotThrow(
            () -> spy.verifyAccess(userId, fileId, Permission.OWNER)
        );

        ArrayList<FileAccessControl> records = mock(ArrayList.class);
        doReturn(true).when(spy).hasAccess(userId, records, Permission.OWNER);
        
        assertDoesNotThrow(
            () -> spy.verifyAccess(userId, records, Permission.OWNER)
        );
    }

    @Test 
    public void hasAccess_returnsFalse_ifAMatchingFileAccessControlsRecordDoesNotExist() {
        when(accessRespository.findByUserIdAndFileId(
            userId, fileId
        ))
        .thenReturn(new ArrayList<>());
        
        assertFalse(
            service.hasAccess(userId, fileId, Permission.GET)
        );
    }
    
    @Test 
    public void verifyAcess_returnsFalse_ifCorrespondingFileAccessControlsHasMismatchOnPermissions() {
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(accessRecord);

        when(accessRecord.getRole()).thenReturn(role);
        when(accessRecord.getUserId()).thenReturn(userId);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.GET));
        when(accessRespository.findByUserIdAndFileId(
            userId, fileId
        ))
        .thenReturn(records);
        
        assertFalse(
            service.hasAccess(userId, fileId, Permission.OWNER)
        );
    }
    
    @Test 
    public void verifyAccess_returnsTrue_ifAccessFileControlsRecord_hasMatchingPermissions() {
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(accessRecord);

        when(accessRecord.getRole()).thenReturn(role);
        when(accessRecord.getUserId()).thenReturn(userId);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWNER));
        when(accessRespository.findByUserIdAndFileId(
            userId, fileId
        ))
        .thenReturn(records);
        
        assertTrue(
            service.hasAccess(userId, fileId, Permission.OWNER)
        );
    }

    @Test 
    public void grantAccess_createsFileAccessControlEntry_ifRequestingUserIsAnOWNER() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWNER);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);
            when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWNER));

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
            Permission.OWNER
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
            .verifyAccess(userId, fileId, Permission.OWNER);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);
            when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWNER));

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
            Permission.OWNER
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
            .verifyAccess(userId, fileId, Permission.OWNER);
            
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(FileAccessControl.from(userId2, fileId, roleId));
        
        when(role.getId()).thenReturn(roleId);
        when(accessRespository.findByFileId(fileId)).thenReturn(records);

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
            .verifyAccess(userId, fileId, Permission.OWNER);
            
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(FileAccessControl.from(userId2, fileId, roleId));
        
        when(role.getId()).thenReturn(roleId);
        when(accessRespository.findByFileId(fileId)).thenReturn(records);
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWNER));

        assertEquals(records.get(0), FileAccessControl.from(userId2, fileId, roleId));
        FileAccessControl found = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found, records.get(0));

        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWNER);
            
        when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
        when(accessRespository.save(any(FileAccessControl.class))).thenReturn(accessRecord);
        FileAccessControl found2 = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found2, accessRecord);
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingUserId_doesNotHaveAnOWNERPermission() throws AccessDeniedException {
        when(role.getRawPermissions()).thenReturn(Set.of(Permission.OWNER));

        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWNER);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.grantAccess(userId, userId2, fileId, role)
        );
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingAnyProvidedParamsDontExist() throws AccessDeniedException {
        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.OWNER);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.verifyAccess(userId, fileId, Permission.OWNER)
        );
    }

    @Test 
    public void grantAccess_throwsAssertionError_ifInitialRoleForFileAccessDoesNotContainOWNER() throws AccessDeniedException {
        when(role.getRawPermissions()).thenReturn(new HashSet<>());
        
        assertThrows(
            AssertionError.class, 
            () -> spy.grantAccess(userId, userId, fileId, role)
        );
    }
}
