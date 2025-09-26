package com.yern.service.storage.gcp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Executable;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.yern.service.storage.BucketImpl;
import com.google.cloud.storage.Bucket;

public class GCSServiceTests {
    private Storage client;
    private GCSService gcs;
    private GCSService spy;
    private final String bucketName = UUID.randomUUID().toString();
    private Bucket bucket;
    private BucketImpl bucketImpl;

    @BeforeEach 
    public void setup() {
        this.client = Mockito.mock(Storage.class);
        this.gcs = new GCSService(this.client);
        this.spy = Mockito.spy(this.gcs);
        this.bucket = Mockito.mock(Bucket.class);
        this.bucketImpl = Mockito.mock(BucketImpl.class);
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
}

@FunctionalInterface
interface StaticMockContext {
    void execute(String message);
}