package com.yern.dto.storage;

import com.yern.model.storage.FileAccessControl;

import io.jsonwebtoken.lang.Assert;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GrantFileAccessResponse {
    private Long id;
    private Long fileId;
    private Long userId;
    private Long roleId;
    private Throwable error;
    
    public static GrantFileAccessResponse from(
        GrantFileAccessRequest req, 
        FileAccessControl access
    ) {
        GrantFileAccessResponse resp = new GrantFileAccessResponse();
        
        Assert.eq(
            req.getFileId(), 
            access.getFileId(), 
            "Requested file and access file must be equal"
        );

        Assert.eq(
            req.getUserId(), 
            access.getUserId(), 
            "Requested user and access user must be equal"
        );

         Assert.eq(
            req.getRoleId(), 
            access.getRole().getId(), 
            "Requested role type and access role type must be equal"
        );

        Assert.gt(
            access.getId(), 
            0L, 
            "File Access Control Id cannot be null"
        );

        resp.setId(access.getId());
        resp.setFileId(access.getFileId());
        resp.setUserId(access.getUserId());
        resp.setRoleId(access.getRole().getId());

        return resp;
    }
}
