package com.yern.service.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.cloud.storage.Bucket;
import com.yern.dto.storage.BucketImpl;

public class BucketImplTests {
    private final String bucketName = UUID.randomUUID().toString();
    
    @Mock 
    private BucketImpl bucket;

    @Mock 
    private Bucket gcsBucket;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void from_aGCSBucket_returnsBucketImplWithSameName() {
        when(gcsBucket.getName()).thenReturn(bucketName);

        BucketImpl foundBucket = BucketImpl.from(gcsBucket);
        assertEquals(foundBucket.getName(), bucketName);
    }
}
