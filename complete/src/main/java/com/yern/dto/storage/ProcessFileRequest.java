package com.yern.dto.storage;

import com.yern.dto.messaging.MessagePayload;
import com.yern.model.storage.FileImpl;
import com.yern.model.storage.StorageProviderType;

import io.jsonwebtoken.lang.Assert;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class ProcessFileRequest implements MessagePayload {
    public static ProcessFileRequest from(FileImpl file) {
        ProcessFileRequest req = new ProcessFileRequest();
        Assert.notNull(file, "File cannot be null");
        Assert.gt(file.getId(), 0L, "File Id must be an integer.");
        Assert.notNull(file.getStorageProvider());

        req.setFileId(file.getId());
        req.setProviderType(file.getStorageProvider());

        return req;
    }

    private StorageProviderType providerType; 
    private Long fileId;
    private String externalId;
}
