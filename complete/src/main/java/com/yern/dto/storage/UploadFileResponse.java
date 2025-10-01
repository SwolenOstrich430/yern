package com.yern.dto.storage;

import com.yern.model.storage.FileImpl;
import com.yern.model.storage.StorageProviderType;
import com.yern.model.storage.UploadFileException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter 
@Getter
public class UploadFileResponse {
    private Long fileId; 
    private String rawPath; 
    private StorageProviderType storageProvider;

    public static UploadFileResponse from(FileImpl file) {
        UploadFileResponse resp = new UploadFileResponse();
        resp.setFileId(file.getId());
        resp.setRawPath(file.getRawPath());
        resp.setStorageProvider(file.getStorageProvider());

        return resp;
    }
}
