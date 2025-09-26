package com.yern.service.storage;

import java.util.List;

public interface CloudStorageProvider extends StorageProvider {
    public BucketImpl createBucket(String bucketName);
    public List<String> listBuckets();
    public BucketImpl getBucket(String path);
    public void deleteBucket(String path);
    public boolean bucketExists(String bucketName);
    public String getFormattedFilePath(String path);
}
