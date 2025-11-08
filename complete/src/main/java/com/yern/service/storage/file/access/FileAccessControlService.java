package com.yern.service.storage.file.access;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import com.yern.dto.storage.GrantFileAccessRequest;
import com.yern.model.security.ResourceType;
import com.yern.model.security.authorization.Permission;
import com.yern.model.security.authorization.Role;
import com.yern.model.security.authorization.RoleType;
import com.yern.model.storage.FileAccessControl;
import com.yern.model.storage.FileImpl;
import com.yern.repository.storage.FileAccessControlRespository;
import com.yern.service.security.authorization.RoleService;

import io.jsonwebtoken.lang.Assert;

@Service
public class FileAccessControlService {
    private FileAccessControlRespository accessRepository;
    private RoleService roleService;
    
    public FileAccessControlService(
        @Autowired FileAccessControlRespository accessRepository,
        @Autowired RoleService roleService
    ) {
        this.accessRepository = accessRepository;
        this.roleService = roleService;
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

    public Optional<FileAccessControl> grantAccess(
        Long requestingUserId,
        GrantFileAccessRequest req
    ) {
        Optional<Role> role = roleService.getRoleById(req.getRoleId());
        Assert.isTrue(role.isPresent());

        return grantAccess(
            requestingUserId,
            req.getUserId(),
            req.getFileId(),
            role.get()
        );
    }

    public Optional<FileAccessControl> grantAccess(
        Long requestingUserId,
        Long requestedUserId,
        Long fileId,
        RoleType roleType
    ) {
        Optional<Role> potentialRole = roleService.getRoleByResourceAndType(
            ResourceType.FILE, 
            roleType
        ); 

        assert(potentialRole.isPresent());

        return grantAccess(
            requestingUserId, 
            requestedUserId, 
            fileId, 
            potentialRole.get()
        );
    }

    public Optional<FileAccessControl> createOwner(
        Long userId,
        Long fileId
    ) throws AccessDeniedException {
        return grantAccess(
            userId, 
            userId, 
            fileId, 
            RoleType.OWNER
        );
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
     * @return **Optional<FileAccessControl>** if the role is granted or already exists.
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
        List<FileAccessControl> currAccess = findByFileId(fileId);
        
        // TODO: convert comments to error messages 
        // Have to assign an OWNER before doing anything else 
        if (currAccess.isEmpty()) {
            assert(requestingUserId == requestedUserId);
            role.validate(RoleType.OWNER, ResourceType.FILE);
        // Can only assign roles with AUTHORIZE
        } else {
            assert(!role.isType(RoleType.OWNER));
            verifyAccess(requestingUserId, fileId, Permission.AUTHORIZE);
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
        return hasAccess(
            userId, 
            findByUserIdAndFileId(userId, fileId), 
            permission
        );
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

    public List<FileAccessControl> findByFileId(Long fileId) {
        FileImpl file = new FileImpl();
        file.setId(fileId);

        return accessRepository.findByFile(file);
    }

     public Page<FileAccessControl> findByUserId(Long userId, Pageable pageable) {
        return accessRepository.findByUserId(userId, pageable);
    }

    public List<FileAccessControl> findByUserIdAndFileId(Long userId, Long fileId) {
        FileImpl file = new FileImpl();
        file.setId(fileId);

        return accessRepository.findByUserIdAndFile(
            userId, file
        );
    }
}
