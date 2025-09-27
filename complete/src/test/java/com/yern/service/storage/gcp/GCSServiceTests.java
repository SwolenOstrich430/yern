package com.yern.service.storage.gcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Executable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.yern.service.storage.BucketImpl;

import io.grpc.internal.Stream;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;

public class GCSServiceTests {
    private Storage client;
    private GCSService gcs;
    private GCSService spy;
    private Bucket bucket;
    private BucketImpl bucketImpl;
    private BlobId blobId;

    private final String bucketName = UUID.randomUUID().toString();
    private final String fileName = UUID.randomUUID().toString();
    private final String folder = UUID.randomUUID().toString();
    private final String fullPath = bucketName + "/" + folder + "/" + fileName;
    private final Path localPath = Mockito.mock(Path.class);

    @BeforeEach 
    public void setup() {
        Paths.get("");
        this.client = Mockito.mock(Storage.class);
        this.gcs = new GCSService(this.client);
        this.spy = Mockito.spy(this.gcs);
        this.bucket = Mockito.mock(Bucket.class);
        this.bucketImpl = Mockito.mock(BucketImpl.class);
        this.blobId = Mockito.mock(BlobId.class);
    }

    @Test
    public void createBucket_createsABucketBasedOnProvideBucketName() {
        BucketInfo bucketInfo = mock(BucketInfo.class);
        
        doReturn(bucketInfo).when(this.spy).getFormattedBucketName(bucketName);
        when(this.client.create(bucketInfo)).thenReturn(bucket);
        when(bucket.getName()).thenReturn(bucketName);

        BucketImpl returnVal = this.spy.createBucket(bucketName);
        verify(this.client, times(1)).create(bucketInfo);
    }

    @Test 
    public void createBucket_returnsTheCreatedBucketAsABucketImplObject() {
        BucketInfo bucketInfo = mock(BucketInfo.class);
        
        doReturn(bucketInfo).when(this.spy).getFormattedBucketName(bucketName);
        when(this.client.create(bucketInfo)).thenReturn(bucket);
        when(bucket.getName()).thenReturn(bucketName);

        BucketImpl returnVal = this.spy.createBucket(bucketName);
        assertInstanceOf(BucketImpl.class, returnVal);
        assertEquals(returnVal.getName(), bucketName);
    }

    @Test 
    public void deleteBucket_deletesTheProvidedBucket() {
        when(client.get(bucketName)).thenReturn(bucket);

        this.gcs.deleteBucket(bucketName);

        Mockito.verify(
            bucket,
            times(1)
        )
        .delete();
    }

    @Test 
    public void downloadFile_downloadsTheProvidedTargetFile_toTheLocalPath() {
        doReturn(this.blobId).when(this.spy).getBlobIdFromPath(fullPath);
        doNothing().when(this.client).downloadTo(blobId, localPath);

        this.spy.downloadFile(localPath, fullPath);

        Mockito.verify(
            this.client,
            times(1)
        )
        .downloadTo(blobId, localPath);
    }

    @Test 
    public void listFiles_returnsAListOfFilePaths_underTheProvidedFolder() {
        Page<Blob> blobs = mock(Page.class);
        
        when(
            this.client.list(eq(bucketName), any())
        )
        .thenReturn(blobs);

        this.spy.listFiles(fullPath);
        
        verify(
            this.client, 
            times(1)
        )
        .list(eq(bucketName), any());
    }
}