package com.yern.dto.storage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.yern.model.storage.FileImpl;
import com.yern.model.storage.StorageProviderType;

public class UploadFileResponseTest {

    @Test  
    public void from_returnsAcceptsFileImpl_returnsUploadResponse() {
        FileImpl file = new FileImpl();
        file.setId(1L);
        file.setRawPath("boop");
        file.setOriginalName("ham.txt");
        file.setStorageProvider(StorageProviderType.GCS);

        UploadFileResponse resp = UploadFileResponse.from(file);
        
        assertInstanceOf(UploadFileResponse.class, resp);
        assertEquals(file.getId(), resp.getFileId());
        assertEquals(file.getRawPath(), resp.getRawPath());
        assertEquals(file.getOriginalName(), resp.getOriginalName());
        assertEquals(file.getStorageProvider(), resp.getStorageProvider());
    }
}
