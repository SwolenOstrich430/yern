package com.yern.dto.storage;

import com.yern.dto.security.authorization.GetRoleResponse;
import com.yern.model.common.AuditTimestamp;
import com.yern.model.storage.ErrorLog;
import com.yern.model.storage.FileAccessControl;
import com.yern.model.storage.FileImpl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.yern.model.security.authorization.Role;

@Getter
@Setter 
@NoArgsConstructor
public class GetFileResponse {
    private Long id;
    private String originalName;
    private String etag;
    private ErrorLog error;
    private AuditTimestamp auditTimestamp;
    private GetRoleResponse role;

    public static GetFileResponse from(FileAccessControl accessControl) {
        FileImpl file = accessControl.getFile();
        GetFileResponse resp = new GetFileResponse();

        resp.setId(file.getId());
        resp.setOriginalName(file.getOriginalName());
        resp.setEtag(file.getEtag());
        resp.setError(file.getError());
        resp.setAuditTimestamp(file.getAuditTimestamps());
        resp.setRole(GetRoleResponse.from(accessControl.getRole()));

        return resp;
    }
}
