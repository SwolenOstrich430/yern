package com.yern.service.storage.file.access;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yern.model.security.Permission;
import com.yern.model.security.Role;
import com.yern.model.storage.FileAccessControl;
import com.yern.repository.storage.FileAccessControlRespository;
import com.yern.service.user.UserService;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class FileAccessControlService {
    private FileAccessControlRespository accessRepository;
    
    public FileAccessControlService(
        @Autowired FileAccessControlRespository accessRepository,
        @Autowired UserService userService
    ) {
        this.accessRepository = accessRepository;
    }

    /** 
     * @param Long fileId: Id of the file attempting to be accessed
     * @param Long userId: Id of the user attempting to access the file
     * @param Permission permission: the kind of access 
     *      
     * @return null if a corresponding entry exists in file_access_controls for the three provided arguments.
     *      
     * @throws AccessDeniedError if the file_access_controls record does not exist for the user or the file
     * @throws AccessDeniedError if a corresponding user_id and file_id do exist in file_access_controls, but there's a mismatch on permission.
     */
    public void verifyAccess(
        Long userId,
        Long fileId,
        Permission permission
    ) throws AccessDeniedException {
        if (!hasAccess(userId, fileId, permission)) {
            throw new AccessDeniedException(
                "Provided user " + 
                userId + " does not have " + 
                permission.name() + " access to file: " + fileId
            );
        }
    }

    protected void verifyAccess(
        Long userId,
        List<FileAccessControl> records,
        Permission permission
    ) throws AccessDeniedException {
        if (!hasAccess(userId, records, permission)) {
            throw new AccessDeniedException(
                "Provided user " + 
                userId + " does not have " + 
                permission.name() + " access"
            );
        }
    }

    /** 
     * @param Long requestingUserId - Id of the user who's adding a role on a file they have AUTHORIZE access to.
     * @param Long requestedUserId - Id of the user who will be given a role on a file.
     * @param Long fileId - the Id of the File (FileImpl) record.
     * @param Role role - the Id of the role 
     * 
     * Creates an entry in file_access_controls if the requestingUser is an OWNER 
     * of the corresponding File. 
     * 
     * If the combination of requestedUserId, fileId, and Role.id 
     * already exist in the table, then the method should return the matching entry
     * without creating a duplicate row. 
     * 
     * Currently, there's nothing preventing a File Owner from adding 
     * another user as an OWNER. There's also no validation on the provided 
     * File and User Ids. Currently an expectation that FileService should be 
     * in charge of validating their existence.
     *      
     * @return **FileAccessControl** if the role is granted or already exists.
     * 
     * @throws AccessDeniedException if the requestingUserId does not have an OWNER role for the provided File.  
     * @throws AccessDeniedException if the Role isn't applicable for files.
     */
    public Optional<FileAccessControl> grantAccess(
        Long requestingUserId,
        Long requestedUserId,
        Long fileId,
        Role role
    ) throws AccessDeniedException {
        List<FileAccessControl> currAccess = accessRepository.findByFileId(fileId);
        
        if (currAccess.isEmpty()) {
            assert(role.getRawPermissions().contains(Permission.OWNER));
        } 
        
        if (requestedUserId != requestingUserId) {
            verifyAccess(requestingUserId, fileId, Permission.OWNER);
        }
        
        FileAccessControl accessRecord = FileAccessControl.from(
            requestedUserId, fileId, role.getId()
        );

        Optional<FileAccessControl> matchingRecord = 
            currAccess
                .stream()
                .filter(record -> record.equals(accessRecord))
                .findFirst();

        if (matchingRecord.isEmpty()) {
            return Optional.of(accessRepository.save(accessRecord));
        } else {
            return matchingRecord;
        }
    }

    /** 
     * @param Long fileId: Id of the file attempting to be accessed
     * @param Long userId: Id of the user attempting to access the file
     * @param Permission permission: the kind of access 
     *      
     * @return null if a corresponding entry exists in file_access_controls for the three provided arguments.
    */   
    public boolean hasAccess(
        Long userId, 
        Long fileId, 
        Permission permission
    ) {
        List<FileAccessControl> records = accessRepository.findByUserIdAndFileId(
            userId, fileId
        );

        return hasAccess(userId, records, permission);
    }

    protected boolean hasAccess(
        Long userId, 
        List<FileAccessControl> records, 
        Permission permission
    ) {
        return records
                .stream()
                .filter(record -> record.getUserId() == userId)
                .anyMatch(record -> {
                    return record
                            .getRole()
                            .getRawPermissions()
                            .contains(permission);
                });
    }
}
