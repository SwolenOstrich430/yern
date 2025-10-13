package com.yern.service.storage.file.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yern.repository.storage.FileAccessControlRespository;

@Service
public class FileAccessControlService {
    private FileAccessControlRespository accessRepository;

    public FileAccessControlService(
        @Autowired FileAccessControlRespository accessRepository
    ) {
        this.accessRepository = accessRepository;
    }

    /**
     * The service is in charge of enforcing user permissions on Files (FileImpl objects). 
     * It references the tables: 
     *  * roles
     *  * users
     *  * files
     * 
     * To determine: 
     *  * what access
     *  * which people have
     *  * on which files 
     * 
     * Permissions in scope are:  
     *  * DELETE
     *  * EDIT
     *  * GET
     * 
     * Roles in scope are: 
     *  * File Owner 
     *      - permissions: 
     *          - DELETE
     *          - EDIT
     *          - GET
     *  * File Editor
     *      - permissions:
     *          - EDIT
     *          - GET
     *  * File Reader
     *      - permissions:
     *          - GET
     * 
     * Roles and Permissions should be registered properly and be upserted automatically 
     * in a way TBD (TODO: figure out db migration). 
     * 
     * TODO: may need to move user service out and accept a user?
     * The service should have the following dependencies: 
     *  * **FileAccessControlRepository**: for accessing the db table. 
     *  * **UserService**: for verifying if users exist or not.
     * 
     * The service should perform the following functions: 
     * @Public 
     *  @method verifyAccess
     *      @param Long fileId: Id of the file attempting to be accessed
     *      @param Long userId: Id of the user attempting to access the file
     *      @param Permission permission: the kind of access 
     *      
     *      @return null if a corresponding entry exists in file_access_controls for the three provided arguments.
     * 
     *      @throws AccessDeniedError if the file_access_controls record does not exist for the user or the file
     *      @throws AccessDeniedError if a corresponding user_id and file_id do exist in file_access_controls, but there's a mismatch on permission.
     * 
     * @method grantRole
     *      @param Long requestingUserId
     *      @param Long requestedUserId
     *      @param Long fileId
     *      @param Role role
     * 
     *      Creates an entry in file_access_controls if the requestingUser is an OWNER 
     *      of the corresponding File. 
     * 
     *      If the combination of requestedUserId, fileId, and Role.id 
     *      already exist in the table, then the method should return without creating 
     *      a duplicate row. 
     * 
     *      If the role is OWNER, the only way that it can be created is if requestingUserId 
     *      and requestedUserId are the same, and no OWNER entry exists in file_access_controls
     *      for the provided File.id. 
     *      
     *      @return **FileAccessControl** if the role is granted.
     * 
     *      @throws AccessDeniedException if the role, file, or users do not exist. 
     *      @throws AccessDeniedException if the requestingUserId does not have an OWNER role for the provided File.  
     *      @throws AccessDeniedException if the Role isn't applicable for files.
     * 
     * @private getByFileIdAndUserId
     *      @param Long fileId
     *      @param Long userId
     * 
     *      @return **Optional<FileAccessControl>**
     * 
     * @private findByFileId
     *      @param Long fileId
     * 
     *      @return **List<FileAccessControl>**
     * 
     * @private findByUserId 
     *      @param Long userId
     * 
     *      @return **List<FileAccessControl>**
     */
}
