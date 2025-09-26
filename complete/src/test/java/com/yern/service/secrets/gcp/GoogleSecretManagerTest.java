package com.yern.service.secrets.gcp;


import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.SecretVersion.State;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.protobuf.ByteString;
import com.yern.service.secrets.SecretImpl;
import com.yern.service.secrets.SecretNotFoundException;

import lombok.experimental.UtilityClass; 

public class GoogleSecretManagerTest {
    private GoogleSecretManager manager;
    private SecretManagerServiceClient client;
    private final Duration maxClientLifeInMinutes = Duration.ofMinutes(1);
    private String projectId; 
    private String fullProjectId;
    private final String versionAlias = "latest";

    @BeforeEach 
    public void setup() throws IOException {
        this.client = mock(SecretManagerServiceClient.class);
        this.projectId = new DefaultGcpProjectIdProvider().getProjectId();
        this.fullProjectId = "projects/" + projectId;

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
    public void getSecret_ifSecretNameMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() throws SecretNotFoundException {
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
    public void getSecret_ifSecretNameAndVersionMatchesAnExistingSecret_ItReturnsTheValueOfThatSecret() {
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
    public void createSecret_ifSecretAlreadyExists_itAddsANewVersionToThatSecret() {
        GoogleSecretManager spy = Mockito.spy(this.manager);

        Secret secret = mock(Secret.class);
        String secretId = UUID.randomUUID().toString();
        String secretVal = UUID.randomUUID().toString();

        doReturn(secret).when(spy).getBaseSecretTemplate();
        Mockito.doNothing().when(spy).addVersion(secretId, secretVal);

        Mockito.when(
            this.client.createSecret(
                this.fullProjectId, 
                secretId, 
                secret
            )
        ).thenThrow(AlreadyExistsException.class);

        assertDoesNotThrow(() -> spy.createSecret(secretId, secretVal));

        Mockito.verify(spy, Mockito.times(1)).addVersion(secretId, secretVal);
    }

    @Test 
    public void deleteSecret_deletesTheSecretAndItsVersions() {
        GoogleSecretManager spy = spy(this.manager);
        DeleteSecretRequest req = mock(DeleteSecretRequest.class);
        String secretName = UUID.randomUUID().toString();

        doReturn(req).when(spy).getDeleteSecretRequest(secretName);
        doNothing().when(this.client).deleteSecret(req);
        spy.deleteSecret(secretName);

        verify(this.client, times(1)).deleteSecret(req);
    }

    @Test 
    public void addVersion_AddsANewVersion_toAnExistingSecret() {
        SecretName secret = mock(SecretName.class);
        String secretName = UUID.randomUUID().toString();
        String secretVal = UUID.randomUUID().toString();
        SecretPayload req = mock(SecretPayload.class);
        GoogleSecretManager spy = spy(this.manager);

        try (MockedStatic<SecretName> mockStatic = 
            mockStatic(SecretName.class)) {
            mockStatic.when(
                () -> SecretName.of(projectId, secretName)
            )
            .thenReturn(secret);
            doReturn(req).when(spy).getCreateSecretPayload(secretVal);

            spy.addVersion(secretName, secretVal);
        }        

        Mockito.verify(
            this.client, 
            times(1)
        )
        .addSecretVersion(secret, req);
    }

    @Test 
    public void deleteVersion_setsSecretVersionStateToDestroyed() {
        String secretName = UUID.randomUUID().toString();
        String secretVal = UUID.randomUUID().toString();
        Optional<String> version = mock(Optional.class);
        SecretVersionName formattedVersion = mock(SecretVersionName.class);
        SecretVersion returnedVersion = mock(SecretVersion.class);

        when(version.orElse(versionAlias)).thenReturn(secretVal);

        try (MockedStatic<SecretVersionName> mockStatic = 
            mockStatic(SecretVersionName.class)) {
            mockStatic.when(
                () -> SecretVersionName.of(projectId, secretName, secretVal)
            )
            .thenReturn(formattedVersion);

            when(
                this.client.destroySecretVersion(formattedVersion)
            )
            .thenReturn(returnedVersion);

            when(returnedVersion.getState()).thenReturn(State.DESTROYED);
            
            this.manager.deleteVersion(secretName, version);

            verify(this.client, times(1)).destroySecretVersion(formattedVersion);
        }        
    }

    @Test 
    public void disableVersion_setsSecretVersionStateToDisabled() {
        String secretName = UUID.randomUUID().toString();
        String secretVal = UUID.randomUUID().toString();
        Optional<String> version = mock(Optional.class);
        SecretVersionName formattedVersion = mock(SecretVersionName.class);
        SecretVersion returnedVersion = mock(SecretVersion.class);

        when(version.orElse(versionAlias)).thenReturn(secretVal);

        try (MockedStatic<SecretVersionName> mockStatic = 
            mockStatic(SecretVersionName.class)) {
            mockStatic.when(
                () -> SecretVersionName.of(projectId, secretName, secretVal)
            )
            .thenReturn(formattedVersion);

            when(
                this.client.disableSecretVersion(formattedVersion)
            )
            .thenReturn(returnedVersion);

            when(returnedVersion.getState()).thenReturn(State.DISABLED);
            
            this.manager.disableVersion(secretName, version);

            verify(this.client, times(1)).disableSecretVersion(formattedVersion);
        }        
    }

    @Test 
    public void enableVersion_setsSecretVersionStateToEnabled() {
        String secretName = UUID.randomUUID().toString();
        String secretVal = UUID.randomUUID().toString();
        Optional<String> version = mock(Optional.class);
        SecretVersionName formattedVersion = mock(SecretVersionName.class);
        SecretVersion returnedVersion = mock(SecretVersion.class);

        when(version.orElse(versionAlias)).thenReturn(secretVal);

        try (MockedStatic<SecretVersionName> mockStatic = 
            mockStatic(SecretVersionName.class)) {
            mockStatic.when(
                () -> SecretVersionName.of(projectId, secretName, secretVal)
            )
            .thenReturn(formattedVersion);

            when(
                this.client.enableSecretVersion(formattedVersion)
            )
            .thenReturn(returnedVersion);

            when(returnedVersion.getState()).thenReturn(State.ENABLED);
            
            this.manager.enableVersion(secretName, version);

            verify(this.client, times(1)).enableSecretVersion(formattedVersion);
        }        
    }

    // TODO: figure this out eventually (delete currently tested in integration test)
    // @Test void getDeleteSecretRequest_returnsDeleteServiceRequest_basedOnProjectIdAndSecretName() {
    //     try (MockedStatic<DeleteSecretRequest> mockedStatic = 
    //             mockStatic(DeleteSecretRequest.class)) {
    //         try (MockedStatic<SecretName> mockStatic = 
    //             mockStatic(SecretName.class)) {
    //             String secretName = UUID.randomUUID().toString();
    //             Builder mockBuilder = mock(Builder.class);
    //             SecretName formattedName = mock(SecretName.class);

    //             mockStatic.when(
    //                 () -> SecretName.of(projectId, secretName)
    //             )
    //             .thenReturn(formattedName);

    //             mockedStatic.when(
    //                 DeleteSecretRequest::newBuilder
    //             )
    //             .thenReturn(mockBuilder);

            
    //             Mockito.verify(mockBuilder, times(1)).setName(secretName);
    //         }
    //     }
    // }

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