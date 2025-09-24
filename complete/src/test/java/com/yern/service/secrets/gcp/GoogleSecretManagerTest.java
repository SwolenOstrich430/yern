package com.yern.service.secrets.gcp;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.protobuf.ByteString;

import com.yern.service.secrets.SecretNotFoundException; 

public class GoogleSecretManagerTest {
    private GoogleSecretManager manager;
    private SecretManagerServiceClient client;
    private final Duration maxClientLifeInMinutes = Duration.ofMinutes(1);
    private String projectId; 
    private final String versionAlias = "latest";

    @BeforeEach 
    public void setup() throws IOException {
        this.client = mock(SecretManagerServiceClient.class);
        this.projectId = new DefaultGcpProjectIdProvider().getProjectId();

        this.manager = new GoogleSecretManager(
            maxClientLifeInMinutes, 
            versionAlias,
            this.client
        );
    }

    @AfterEach 
    public void tearDown() {
        this.client.close();
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

        try {
            spy.getClient().close();
        } catch (Exception e) {}
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
    public void get_ifSecretNameMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() throws IOException, SecretNotFoundException {
        String secretName = UUID.randomUUID().toString();
        Optional<String> version = Optional.empty();

        GoogleSecretManager spy = Mockito.spy(this.manager);
        AccessSecretVersionRequest req = mock(AccessSecretVersionRequest.class);
        
        doReturn(req).when(spy).getSecretAccessRequest(
            secretName,
            version
        );
        
        AccessSecretVersionResponse resp = mock(AccessSecretVersionResponse.class);
        Mockito.when(client.accessSecretVersion(req)).thenReturn(resp);

        SecretPayload payload = mock(SecretPayload.class);
        Mockito.when(resp.getPayload()).thenReturn(payload);
        ByteString respStr = mock(ByteString.class);

        Mockito.when(payload.getData()).thenReturn(respStr);
        Mockito.when(respStr.toString()).thenReturn(secretName);

        assertEquals(
            spy.get(secretName, version),
            secretName
        );
    }

    @Test 
    public void get_ifVersionIsEmpty_ItReturnsTheLatestVersionOfThatSecret() {

    }

    @Test 
    public void get_ifSecretNameAndVersionMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() {
        
    }

    @Test 
    public void get_ifSecretNameDoesNotMatchAnExistingSecret_ItThrowsSecretNotFoundException() {

    }

    @Test 
    public void get_ifSecretNameMatchesAnExistingSecretButVersionDoesNotExist_ItThrowsSecretNotFoundException() {

    }

    @Test 
    public void create_createsANewSecret() throws IOException {
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

        GoogleSecretManager spy = Mockito.spy(this.manager);

        spy.createClient();

        Mockito.verify(
            spy, 
            Mockito.times(1)
        ).closeClient();
    }

    @Test 
    public void isClientExpired_returnsTrue_whenClientIsShutdown() throws IOException {
        Mockito.when(this.manager.getClient().isShutdown()).thenReturn(true);

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