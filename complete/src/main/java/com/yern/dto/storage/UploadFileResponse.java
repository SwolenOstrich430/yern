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
    private String originalName;
    private String error;

    public static UploadFileResponse from(FileImpl file) {
        UploadFileResponse resp = new UploadFileResponse();
        resp.setFileId(file.getId());
        resp.setRawPath(file.getRawPath());
        resp.setStorageProvider(file.getStorageProvider());
        resp.setOriginalName(file.getOriginalName());

        return resp;
    }

    public static UploadFileResponse from(UploadFileException exc) {
        UploadFileResponse resp = new UploadFileResponse();
        resp.setError(exc.getMessage());
        return resp;
    }
}
