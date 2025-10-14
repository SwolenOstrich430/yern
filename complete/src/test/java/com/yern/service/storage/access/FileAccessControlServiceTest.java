package com.yern.service.storage.access;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;

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
    public void grantAccess_createsFileAccessControlEntry_ifRequestingUserIsAnOWNER() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);

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
    public void grantAccess_createsFileAccessControlEntry_ifTheFileDoesNotHaveAnyEntriesInFileAccessControlsAndUserIdsAreTheSame() throws AccessDeniedException {
        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        try (MockedStatic<FileAccessControl> mockedStatic = Mockito.mockStatic(FileAccessControl.class)) {
            when(role.getId()).thenReturn(roleId);
            when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
            when(accessRespository.save(accessRecord)).thenReturn(accessRecord);

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
            Permission.AUTHORIZE
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
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        ArrayList<FileAccessControl> records = new ArrayList<>();
        records.add(FileAccessControl.from(userId2, fileId, roleId));
        
        when(role.getId()).thenReturn(roleId);
        when(accessRespository.findByFileId(fileId)).thenReturn(records);

        assertEquals(records.get(0), FileAccessControl.from(userId2, fileId, roleId));
        FileAccessControl found = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found, records.get(0));

        doNothing()
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);
            
        when(accessRespository.findByFileId(fileId)).thenReturn(new ArrayList<>());
        when(accessRespository.save(any(FileAccessControl.class))).thenReturn(accessRecord);
        FileAccessControl found2 = spy.grantAccess(userId, userId2, fileId, role).get();
        assertEquals(found2, accessRecord);
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingUserId_doesNotHaveAnAuthorizePermission() throws AccessDeniedException {
        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.verifyAccess(userId, fileId, Permission.AUTHORIZE)
        );
    }

    @Test 
    public void grantAccess_throwsAcessDeniedError_ifRequestingAnyProvidedParamsDontExist() throws AccessDeniedException {
        doThrow(AccessDeniedException.class)
            .when(spy)
            .verifyAccess(userId, fileId, Permission.AUTHORIZE);

        assertThrows(
            AccessDeniedException.class, 
            () -> spy.verifyAccess(userId, fileId, Permission.AUTHORIZE)
        );
    }
}
