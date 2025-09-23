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

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.protobuf.ByteString;


import java.time.LocalDateTime;
import java.util.UUID;
import java.io.IOException;
import java.time.Duration;
 

public class GoogleSecretManagerTest {
    private GoogleSecretManager manager;
    private SecretManagerServiceClient client;
    private final Duration maxClientLifeInMinutes = Duration.ofMinutes(1);
    private final String projectId = UUID.randomUUID().toString();

    @BeforeEach 
    public void setup() {
        this.client = mock(SecretManagerServiceClient.class);
        this.manager = new GoogleSecretManager(maxClientLifeInMinutes, projectId);
        this.manager.setClient(client);
    }

    @Test
    public void setClient_setsClient_whenClientIsNull() throws IOException {
        this.manager.setClient(null);
        assertNull(manager.getClient());    

        manager.setClient();
        assertInstanceOf(
            SecretManagerServiceClient.class, 
            manager.getClient()
        );
    }

    @Test
    public void setClient_setsClient_whenClientIsExpired() throws IOException {
        GoogleSecretManager spy = Mockito.spy(manager);

        spy.setClient();
        assertInstanceOf(
            SecretManagerServiceClient.class, 
            spy.getClient()
        );
        
        doReturn(true).when(spy).isClientExpired();
        spy.setClient();
        
        assertInstanceOf(
            SecretManagerServiceClient.class,
            spy.getClient()
        );

        assertNotEquals(
            this.client,
            spy.getClient(),
            "Client after expiration should be set to a new client, but it wasn't."
        );
    }

    @Test 
    public void setClient_setsLastClientResetAt_toCurrentTime() throws InterruptedException {
        LocalDateTime oldReset = this.manager.getLastClientResetAt();
        
        Thread.sleep(10);
        this.manager.setLastClientResetAt();

        LocalDateTime newReset = this.manager.getLastClientResetAt();
        assertTrue(newReset.isAfter(oldReset));
    }

    @Test 
    public void create_createsANewSecret() {
        GoogleSecretManager spy = Mockito.spy(this.manager);

        String secretId = "1";
        String secretVal = "boop";
        Secret secret = mock(Secret.class);
        SecretPayload payload = mock(SecretPayload.class);

        Mockito.when(
            this.client.createSecret(
                this.projectId, 
                secretId, 
                secret
            )
        ).thenReturn(secret);
        Mockito.when(
            secret.getName()
        ).thenReturn(secretId);
        doReturn(secret).when(spy).getBaseSecretTemplate();
        doReturn(payload).when(spy).getCreateSecretPayload(secretVal);

        spy.create(secretId, secretVal);

        Mockito.verify(
            this.client, 
            Mockito.times(1)
        ).createSecret(
            this.projectId,
            secretId,
            secret
        );

        Mockito.verify(
            this.client,
            Mockito.times(1)
        ).addSecretVersion(
            secretId,
            payload
        );
    }

    @Test 
    public void getBaseSecretTemplate_returnsASecret_withAutoamticReplication() {
        Secret secret = this.manager.getBaseSecretTemplate();
        assertInstanceOf(Secret.class, secret);
        // javadoc: https://cloud.google.com/java/docs/reference/google-cloud-secretmanager/2.3.0/com.google.cloud.secretmanager.v1beta1.Replication#com_google_cloud_secretmanager_v1beta1_Replication_hasAutomatic__
        assertTrue(secret.getReplication().hasAutomatic());
    }

    @Test 
    public void getCreateSecretPayload_returnsASecret_withTheProvidedStringAsItsValue() {
        String secret = "hello";
        SecretPayload payload = this.manager.getCreateSecretPayload(secret);

        assertInstanceOf(SecretPayload.class, payload);
        assertEquals(payload.getData(), ByteString.copyFromUtf8(secret));
    }

    @Test 
    public void createClient_ifCurrClientIsNotNull_itClosesTheClientBeforeSettingANewOne() throws IOException {
        assertInstanceOf(
            SecretManagerServiceClient.class, 
            client
        );

        manager.createClient();

        Mockito.verify(
            this.client, 
            Mockito.times(1)
        ).close();
    }

    @Test 
    public void isClientExpired_returnsTrue_whenClientIsShutdown() throws IOException {
        Mockito.when(this.client.isShutdown()).thenReturn(true);

        assertTrue(
            this.manager.isClientExpired()
        );
    }

    @Test 
    public void isClientExpired_returnsFalse_whenClientIsNotShutdown() throws IOException {
        Mockito.when(this.client.isShutdown()).thenReturn(false);

        assertFalse(
            this.manager.isClientExpired()
        );
    }

    @Test 
    public void isClientExpired_returnsTrue_clientHasBeenAliveLongerThanMaxClientLifeInMinutes() throws IOException {
        Mockito.when(this.client.isShutdown()).thenReturn(false);

        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(
                this.manager.getLastClientResetAt().plus(
                    this.manager.getMaxClientLifeInMinutes().plus(
                        Duration.ofSeconds(1)
                    )
                )
            );
            
            assertTrue(
                this.manager.isClientExpired()
            );
        }
    }

    @Test 
    public void isClientExpired_returnsTrue_clientHasBeenAliveAsLongAsMaxClientLifeInMinutes() throws IOException {
        Mockito.when(this.client.isShutdown()).thenReturn(false);

        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(
                this.manager.getLastClientResetAt().plus(
                    this.manager.getMaxClientLifeInMinutes()
                )
            );
            
            assertTrue(
                this.manager.isClientExpired()
            );
        }
    }

    @Test 
    public void isClientExpired_returnsFalse_clientHasBeenAliveLessThanMaxClientLifeInMinutes() throws IOException {
        Mockito.when(this.client.isShutdown()).thenReturn(false);

        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(
                this.manager.getLastClientResetAt().plus(
                    this.manager.getMaxClientLifeInMinutes().minus(Duration.ofSeconds(1))
                )
            );
            
            assertFalse(
                this.manager.isClientExpired()
            );
        }
    }
}