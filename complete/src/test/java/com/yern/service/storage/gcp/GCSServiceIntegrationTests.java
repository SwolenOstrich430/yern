package com.yern.service.storage.gcp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.cloud.storage.Storage;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.storage.BucketImpl;

@SpringBootTest(classes=RestServiceApplication.class)
@TestMethodOrder(OrderAnnotation.class)
public class GCSServiceIntegrationTests {
    @Autowired 
    private Storage client;
    private GCSService service; 
    private List<String> crudBuckets; 
    private String bucketName;

    @BeforeEach
    public void setup() {
        assertNotNull(client);
        assert(client instanceof Storage);

        if(service == null) {
            service = new GCSService(client);
        }

        if (crudBuckets == null) {
            crudBuckets = new ArrayList<>();
        }

        crudBuckets.add(UUID.randomUUID().toString());
        bucketName = UUID.randomUUID().toString();
        crudBuckets.add(bucketName);

        for(BucketImpl existingBucket : service.listBuckets()) {
            service.deleteBucket(existingBucket.getName());
        }
    }

    @Test 
    @Order(1)
    public void crudBucket() {
        /**
         * ensure bucketExists returns false for all bucket names 
         * create a list of buckets 
         * create another bucket (main bucket)
         * ensure bucketExists returns true for all bucket names 
         * list buckets and make sure all return 
         * get each individual bucket 
         * delete each bucket but the main one 
         */
        for (String bucket : crudBuckets) {
            assertFalse(service.bucketExists(bucket));
            service.createBucket(bucket);
            assertTrue(service.bucketExists(bucket));
        }

        List<BucketImpl> foundBuckets = service.listBuckets();
        assertEquals(foundBuckets.size(), crudBuckets.size());

        for(BucketImpl foundBucket : foundBuckets) {
            assertTrue(crudBuckets.contains(foundBucket.getName()));
            BucketImpl matchingBucket = service.getBucket(foundBucket.getName());
            assertEquals(matchingBucket, foundBucket);

            if (!(foundBucket.getName().equals(bucketName))) {
                service.deleteBucket(foundBucket.getName());
                assertFalse(service.bucketExists(foundBucket.getName()));
            }
        }
    }
}
