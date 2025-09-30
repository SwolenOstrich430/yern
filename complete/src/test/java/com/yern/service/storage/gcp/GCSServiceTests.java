package com.yern.service.storage.gcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BlobInfo.Builder;
import com.yern.dto.storage.BucketImpl;
import com.yern.service.storage.cloud.gcp.GCSService;
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

        this.spy.createBucket(bucketName);
        
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
    public void getBucket_returnsABucketImpl_fromTheFoundBucket() {
        try (MockedStatic<BucketImpl> mockedStatic = Mockito.mockStatic(BucketImpl.class)) {
            when(client.get(bucketName)).thenReturn(bucket);
            mockedStatic.when(() -> BucketImpl.from(bucket)).thenReturn(bucketImpl);

            BucketImpl foundBucket = spy.getBucket(bucketName);

            assertEquals(foundBucket, bucketImpl);
        }
    }

    @Test 
    public void listBuckets_returnsAListOfBucketImpl() {
        Page<Bucket> buckets = mock(Page.class);
        
        when(
            this.client.list()
        )
        .thenReturn(buckets);

        List<BucketImpl> foundBuckets = this.spy.listBuckets();
        for (BucketImpl currBucket : foundBuckets) {
            assertInstanceOf(BucketImpl.class, currBucket);
        }

        verify(
            this.client, 
            times(1)
        )
        .list();
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
    public void bucketExists_returnsTrue_whenClientGetReturnsABucketThatExists() {
        when(client.get(bucketName)).thenReturn(bucket);
        when(bucket.exists()).thenReturn(true);

        assertTrue(gcs.bucketExists(bucketName));
    }

    @Test 
    public void bucketExists_returnsTrue_whenClientGetReturnsABucketThatDoesNotExists() {
        when(client.get(bucketName)).thenReturn(bucket);
        when(bucket.exists()).thenReturn(false);

        assertFalse(gcs.bucketExists(bucketName));
    }

    @Test 
    public void bucketExists_returnsTrue_whenClientGetReturnsNull() {
        when(client.get(bucketName)).thenReturn(null);
        assertFalse(gcs.bucketExists(bucketName));
    }

    @Test 
    public void downloadFile_downloadsTheProvidedTargetFile_toTheLocalPath() throws IOException {
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
    public void uploadFile_createsAFileInGCS_basedOnFileAtLocalPath() throws IOException {
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        Builder builder = mock(Builder.class);
        BlobInfo blobInfo = mock(BlobInfo.class);

        try (MockedStatic<BlobInfo> mockedStatic = Mockito.mockStatic(BlobInfo.class)) {
            mockedStatic.when(
                () -> BlobInfo.newBuilder(any())
            ).thenReturn(builder);
            when(builder.build()).thenReturn(blobInfo);
            
            this.spy.uploadFile(localPath, fullPath);

            verify(
                client, 
                times(1)
            )
            .createFrom(blobInfo, localPath);
        }

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

    @Test 
    public void deleteFile_doesNotError_ifFileIsDeleted() {
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.delete(blobId)).thenReturn(true);

        assertDoesNotThrow(() -> spy.deleteFile(fullPath));
        
        verify(
            client, 
            times(1)
        )
        .delete(blobId);
    }

    @Test 
    public void deleteFile_doesErrors_ifFileIsNotDeleted() {
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.delete(blobId)).thenReturn(false);

        assertThrows(
            FileNotFoundException.class,
            () -> spy.deleteFile(fullPath)
        );
        
        verify(
            client, 
            times(1)
        )
        .delete(blobId);
    }

    @Test 
    public void copyFile_copiesCurrentPathToTargetPath() throws FileNotFoundException {
        String targetPath = UUID.randomUUID().toString();
        BlobId targetFileId = mock(BlobId.class);
        Blob blob = mock(Blob.class);

        doReturn(targetFileId).when(spy).getBlobIdFromPath(targetPath);
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.get(blobId)).thenReturn(blob);
        doReturn(true).when(spy).fileExists(targetPath);

        spy.copyFile(fullPath, targetPath);

        verify(blob, times(1)).copyTo(targetFileId);
    }

    @Test 
    public void moveFile_copiesTheCurrentFileToTarget_andDeletesTheCurrentFile() throws IOException {
        String targetPath = UUID.randomUUID().toString();
        doNothing().when(spy).copyFile(targetPath, fullPath);
        doNothing().when(spy).deleteFile(targetPath);
        InOrder inOrder = inOrder(spy, spy);

        spy.moveFile(targetPath, fullPath);

        inOrder.verify(
            spy, times(1)
        )
        .copyFile(targetPath, fullPath);

        inOrder.verify(
            spy, 
            times(1)
        )
        .deleteFile(targetPath);
    }

    @Test 
    public void fileExists_returnsTrue_whenGetFileIsNotNullAndExists() {
        Blob blob = mock(Blob.class);
        
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.get(blobId)).thenReturn(blob);
        when(blob.exists()).thenReturn(true);

        assertTrue(spy.fileExists(fullPath));
    }

    @Test 
    public void fileExists_returnsFalse_whenGetFileIsNull() {
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.get(blobId)).thenReturn(null);

        assertFalse(spy.fileExists(fullPath));
    }

    @Test 
    public void fileExists_returnsFalse_whenGetFileIsNotNulButDoesntExist() {
        Blob blob = mock(Blob.class);
        
        doReturn(blobId).when(spy).getBlobIdFromPath(fullPath);
        when(client.get(blobId)).thenReturn(blob);
        when(blob.exists()).thenReturn(false);

        assertFalse(spy.fileExists(fullPath));
    }
}