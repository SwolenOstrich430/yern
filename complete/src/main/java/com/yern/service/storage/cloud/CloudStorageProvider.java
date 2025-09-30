package com.yern.service.storage.cloud;

import java.util.List;

import com.yern.dto.storage.BucketImpl;
import com.yern.service.storage.StorageProvider;

public interface CloudStorageProvider extends StorageProvider {
    public BucketImpl createBucket(String bucketName);
    public List<BucketImpl> listBuckets();
    public BucketImpl getBucket(String path);
    public void deleteBucket(String path);
    public boolean bucketExists(String bucketName);
    public String getFormattedFilePath(String path);
}
