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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.yern.service.secrets.SecretImpl;
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
    public void get_ifSecretNameMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() throws SecretNotFoundException {
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
        SecretImpl parsedSecret = mock(SecretImpl.class);
        doReturn(parsedSecret).when(spy).parseAccessSecretResponse(resp, secretName);

        assertEquals(
            spy.getSecret(secretName, version),
            parsedSecret
        );
    }

    @Test 
    public void get_ifSecretNameAndVersionMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() {
        String secretName = UUID.randomUUID().toString();
        String rawSecret = "1";
        Optional<String> version = Optional.of(rawSecret);

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
        SecretImpl foundSecret = mock(SecretImpl.class);
        doReturn(foundSecret).when(spy).parseAccessSecretResponse(resp, secretName);

        assertEquals(
            spy.getSecret(secretName, version),
            foundSecret
        );
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
        Mockito.doNothing().when(spy).addVersion(secretId, secretVal);

        spy.createSecret(secretId, secretVal);

        Mockito.verify(
            this.client, 
            Mockito.times(1)
        ).createSecret(
            "projects/" + this.projectId,
            secretId,
            secret
        );

        Mockito.verify(
            spy,
            Mockito.times(1)
        ).addVersion(
            secretId,
            secretVal
        );
    }

    @Test 
    public void parseAccessSecretResponse_ifResponseHasPayload_itReturnsASecretImplObject() {
        String secretName = UUID.randomUUID().toString();
        GoogleSecretManager spy = Mockito.spy(this.manager);
        AccessSecretVersionResponse resp = mock(AccessSecretVersionResponse.class);

        SecretPayload payload = mock(SecretPayload.class);
        Mockito.when(resp.getPayload()).thenReturn(payload);
        ByteString respStr = mock(ByteString.class);

        Mockito.when(payload.getData()).thenReturn(respStr);
        Mockito.when(respStr.toStringUtf8()).thenReturn(secretName);
        Mockito.when(resp.hasPayload()).thenReturn(true);
        Mockito.when(resp.getName()).thenReturn(secretName);

        SecretImpl parsedSecret = spy.parseAccessSecretResponse(resp, secretName);

        assertEquals(parsedSecret.getName(), secretName);
        assertEquals(parsedSecret.getValue(), secretName);
    }

    @Test 
    public void disable_disablesTheProvidedSecretVersion() {

    }

    @Test 
    public void parseAccessSecretResponse_ifResponseDoesntHavePayload_throwsSecretNotFoundException()  throws SecretNotFoundException {
        String secretName = UUID.randomUUID().toString();
        GoogleSecretManager spy = Mockito.spy(this.manager);
        AccessSecretVersionResponse resp = mock(AccessSecretVersionResponse.class);

        Mockito.when(resp.hasPayload()).thenReturn(false);

        assertThrows(
            SecretNotFoundException.class,
            () -> spy.parseAccessSecretResponse(resp, secretName) // The code that is expected to throw the exception
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
}