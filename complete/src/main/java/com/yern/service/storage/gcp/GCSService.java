package com.yern.service.storage.gcp;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.yern.service.storage.BucketImpl;
import com.yern.service.storage.CloudStorageProvider;

// TODO: 
// * exception handling for: createSecret, deleteSecret
public class GCSService implements CloudStorageProvider {
    private Storage client;

    public GCSService(
        @Autowired Storage client
    ) {
        this.client = client; 
    }

    @Override
    public BucketImpl createBucket(String bucketName) {
        Bucket bucket = client.create(
            getFormattedBucketName(bucketName)
        )
        ;
        return BucketImpl.from(bucket);
    }

    @Override
    public List<String> listBuckets() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listBuckets'");
    }

    @Override
    public BucketImpl getBucket(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBucket'");
    }

    @Override
    public void deleteBucket(String bucketName) {
        Bucket bucket = client.get(bucketName);
        bucket.delete();
    }

    @Override
    public boolean bucketExists(String bucketName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bucketExists'");
    }

    @Override
    public boolean fileExists(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fileExists'");
    }

    @Override
    public void createFolder(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createFolder'");
    }

    @Override
    public List<String> listFolders(String folderPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listFolders'");
    }

    @Override
    public void deleteFolder(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteFolder'");
    }

    @Override
    public void uploadFile(String localPath, String targetPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadFile'");
    }

    @Override
    public File downloadFile(String localPath, String targetPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloadFile'");
    }

    @Override
    public void updateFile(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateFile'");
    }

    @Override
    public void deleteFile(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteFile'");
    }

    @Override
    public void copyFile(String currentPath, String targetPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'copyFile'");
    }

    @Override
    public void moveFile(String curreString, String targetPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'moveFile'");
    }

    @Override
    public String getFormattedFilePath(String path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFormattedFilePath'");
    }

    public BucketInfo getFormattedBucketName(String bucketName) {
        return BucketInfo.newBuilder(bucketName).build();
    }
}
