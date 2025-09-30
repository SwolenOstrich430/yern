package com.yern.service.storage.gcp;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.cloud.storage.Storage;
import com.yern.dto.storage.BucketImpl;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.storage.cloud.gcp.GCSService;

@SpringBootTest(classes=RestServiceApplication.class)
@TestMethodOrder(OrderAnnotation.class)
public class GCSServiceIntegrationTests {
    @Autowired 
    private Storage client;
    private GCSService service; 
    private List<String> crudBuckets; 
    private String bucketName;
    private String fileName;

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

        fileName = UUID.randomUUID().toString();
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

            service.deleteBucket(foundBucket.getName());
            assertFalse(service.bucketExists(foundBucket.getName()));
        }
    }

    // TODO: add tests for verifying content on download 
    @Test
    @Order(2)
    public void crudFiles() throws IOException {
        service.createBucket(bucketName);
        assertTrue(service.bucketExists(bucketName));

        File file = new File(fileName); 
        Path localPath = Path.of(file.getPath());
        file.createNewFile();
        assert(file.exists());

        String cloudPath = bucketName + "/" + file.getName();
        assertFalse(service.fileExists(cloudPath));
        service.uploadFile(localPath, cloudPath);
        assertTrue(service.fileExists(cloudPath));

        List<String> createdFiles = service.listFiles(bucketName);
        assertEquals(createdFiles.size(), 1);
        assertTrue(createdFiles.contains(fileName));

        file.delete();
        assertFalse(file.exists());
        File newFile = localPath.toFile();
        assertFalse(newFile.exists());

        service.downloadFile(localPath, cloudPath);
        assertTrue(newFile.exists());

        newFile.delete();
        assertFalse(newFile.exists());

        String newCloudFile = bucketName + "/" + UUID.randomUUID().toString();
        assertFalse(service.fileExists(newCloudFile));
        service.copyFile(cloudPath, newCloudFile);
        assertTrue(service.fileExists(newCloudFile));

        service.deleteFile(cloudPath);
        assertFalse(service.fileExists(cloudPath));
        service.moveFile(newCloudFile, cloudPath);
        assertFalse(service.fileExists(newCloudFile));
        assertTrue(service.fileExists(cloudPath));

        service.deleteFile(cloudPath);
        assertFalse(service.fileExists(cloudPath));
    }
}
