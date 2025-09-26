package com.yern.service.secrets.gcp;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.yern.restservice.RestServiceApplication;
import com.yern.service.secrets.SecretImpl;
import com.yern.service.secrets.SecretNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@SpringBootTest(classes = RestServiceApplication.class)
@TestMethodOrder(OrderAnnotation.class)
public class GoogleSecretManagerIntegrationTest {
    @Autowired
    private SecretManagerServiceClient client;
    @Value("${secrets.google.client.max-life-in-minutes}") 
    private Duration maxClientLifeInMinutes;
    @Value("${secrets.google.latest-version-alias}")
    private String latestVersionAlias;
     private GoogleSecretManager manager;
    private HashMap<String, String> secretsByNames;


    @BeforeEach 
    public void setup() throws IOException {
        if (this.manager == null) {
            this.manager = new GoogleSecretManager(
                this.maxClientLifeInMinutes, 
                this.latestVersionAlias, 
                this.client
            );
        }

        if (secretsByNames == null) {
            secretsByNames = new HashMap<>();
        } 
        
        if ((secretsByNames.isEmpty())) {
            secretsByNames.put("boop", "bop");
            secretsByNames.put("boop1", "bop1");
        }
    }

    @Test 
    @Order(1)
    public void createInitialSecretVersions() throws IOException {
        for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
            assertDoesNotThrow(() -> manager.createSecret(entry.getKey(), entry.getValue()));
        }
    }

    @Test 
    @Order(2)
    public void getInitialSecrets() throws IOException {
        SecretImpl foundSecret; 

        for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
            foundSecret = this.manager.getSecret(entry.getKey(), Optional.empty());

            assertEquals(foundSecret.getName(), entry.getKey());
            assertEquals(foundSecret.getValue(), entry.getValue());
            // assertEquals(foundSecret.getVersion(), 1);
        }
    }

    @Test 
    @Order(3)
    public void disableEnableDestroyInitialSecretVersions() {
        for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
            SecretImpl foundSecret = this.manager.getSecret(entry.getKey(), Optional.empty());
            Optional<String> version = Optional.of(foundSecret.getVersion());
            assertDoesNotThrow(() -> {
                this.manager.disableVersion(entry.getKey(), version);
                this.manager.enableVersion(entry.getKey(), version);
                this.manager.deleteVersion(entry.getKey(), version);
            });
        }
    }

    @Test 
    @Order(4)
    public void deleteAllSecrets() {
        for(Map.Entry<String, String>entry : secretsByNames.entrySet()) {
            assertDoesNotThrow(() -> {
                this.manager.deleteSecret(entry.getKey());
            });

            assertThrows(
                SecretNotFoundException.class, 
                () -> this.manager.getSecret(entry.getKey(), Optional.empty())
            );
        }
    }
}
