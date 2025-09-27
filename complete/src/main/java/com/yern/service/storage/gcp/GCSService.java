package com.yern.service.storage.gcp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.common.collect.Streams;
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
    public void uploadFile(Path localPath, String targetPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadFile'");
    }

    @Override
    public void downloadFile(Path localPath, String targetPath) {
        this.client.downloadTo(
            getBlobIdFromPath(targetPath), 
            localPath
        );
    }

    @Override 
    public List<String> listFiles(String path) {
        Page<Blob> blobs = client.list(
            getBucketNameFromPath(path),
            BlobListOption.prefix(getFileNameFromPath(path))
        );

        return blobs.streamAll().map(Blob::getName).toList();
    }

    public BlobId getBlobIdFromPath(String path) {
        return BlobId.of(
            getBucketNameFromPath(path), 
            getFileNameFromPath(path)
        );
    }

    private String getBucketNameFromPath(String path) {
        return path.split("/")[0];
    }

    private String getFileNameFromPath(String path) {
        String[] splitPath = 
            Arrays.stream(path.split("/"))
                    .skip(1)
                    .toArray(String[]::new);
        
        return String.join("/", splitPath);
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
