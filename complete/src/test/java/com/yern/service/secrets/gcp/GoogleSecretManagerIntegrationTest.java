package com.yern.service.secrets.gcp;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

@SpringBootTest(classes = GoogleSecretManager.class)
public class GoogleSecretManagerIntegrationTest {
    @Autowired
    private GoogleSecretManager manager;

    /**
     * 1. Create secrets
     * 2. Get individual secrets 
     * 3. Disable a secret 
     * 4. Delete a secret 
     */
    // @Test 
    // @Order(1)
    // public void create() {
        
    // }
}
