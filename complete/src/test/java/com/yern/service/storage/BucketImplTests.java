package com.yern.service.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.cloud.storage.Bucket;
import com.yern.dto.storage.BucketImpl;

public class BucketImplTests {
    private BucketImpl bucket;
    private final String bucketName = UUID.randomUUID().toString();

    @BeforeEach
    public void setup() {
        this.bucket = new BucketImpl(bucketName);
    }

    @Test
    public void from_aGCSBucket_returnsBucketImplWithSameName() {
        Bucket gcsBucket = Mockito.mock(Bucket.class);
        Mockito.when(gcsBucket.getName()).thenReturn(bucketName);

        BucketImpl foundBucket = BucketImpl.from(gcsBucket);
        assertEquals(foundBucket.getName(), bucketName);
    }
}
