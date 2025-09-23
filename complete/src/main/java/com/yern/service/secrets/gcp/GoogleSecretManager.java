package com.yern.service.secrets.gcp;

import com.yern.service.secrets.SecretNotFoundException;
import com.yern.service.secrets.SecretAlreadyExistsException;
import com.yern.service.secrets.SecretManager;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.protobuf.ByteString;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Implementation of the SecretsManager interface for Google. Idea is to be able to switch between
 * different secret managers easily if we move to a different cloud provider.
 */
@Service
public class GoogleSecretManager implements SecretManager {
    private SecretManagerServiceClient client;
    private Duration maxClientLifeInMinutes;
    private LocalDateTime lastClientResetAt;
    // TODO: this should be returned dynamically in another story 
    private String projectId;

    public GoogleSecretManager(
        @Value("${cloud.gcp.project-id}") String projectId
    ) {
        this.projectId = projectId;
    }
    
    public GoogleSecretManager(
        @Value("${secrets.google.client.max-life-in-minutes}") 
        Duration maxClientLifeInMinutes,
        @Value("${cloud.gcp.project-id}")
        String projectId
    ) {
        this.maxClientLifeInMinutes = maxClientLifeInMinutes;
        this.projectId = projectId;
    }

    /**
     * Sets 
     */
    public void setClient() throws IOException {
        if ((this.client == null) || this.isClientExpired()) {
            this.setLastClientResetAt();
            this.client = this.createClient();
        }
    }

    public void setClient(SecretManagerServiceClient newClient) {
        this.client = newClient;
        this.setLastClientResetAt();
    }

    public SecretManagerServiceClient getClient() {
        return client;
    }

    public String get(String secretName, Optional<String> version) throws SecretNotFoundException {
        return "";
    }

    public void create(String secretName, String secret) throws SecretAlreadyExistsException {
        Secret baseSecret = this.getBaseSecretTemplate();
        Secret createdSecret = this.client.createSecret(
            this.projectId, secretName, baseSecret
        );

        SecretPayload payload = getCreateSecretPayload(secret);

        client.addSecretVersion(
            createdSecret.getName(), 
            payload
        );
    }

    public void delete(String secretName, Optional<String> version) throws RuntimeException {

    }

    public void disable(String secretName) throws RuntimeException {

    }

    public Secret getBaseSecretTemplate() {
        return Secret
                .newBuilder()
                .setReplication(
                    Replication.newBuilder()
                        .setAutomatic(
                            Replication.Automatic.newBuilder().build()
                    )
                    .build()
                )
                .build();
    }

    public SecretPayload getCreateSecretPayload(String secret) {
        return SecretPayload
                    .newBuilder()
                    .setData(
                        ByteString.copyFromUtf8(secret)
                    )
                    .build();
    }

    public SecretManagerServiceClient createClient() throws IOException {
        if (this.client != null) {
            this.client.close();
        }

        return SecretManagerServiceClient.create();
    }

    public boolean isClientExpired() {        
        return (
            this.client.isShutdown() || 
            this.hasClientReachedTtl()
        );
    }

    public boolean hasClientReachedTtl() {
        if (!(this.lastClientResetAt instanceof LocalDateTime) || (this.lastClientResetAt == null)) {
            return false;
        }

        LocalDateTime currTime = LocalDateTime.now();
        LocalDateTime expiry = this.lastClientResetAt.plus(
            this.maxClientLifeInMinutes
        );

        return (
            currTime.isAfter(expiry) || currTime.isEqual(expiry)
        );
    }

    public Duration getMaxClientLifeInMinutes() {
        return this.maxClientLifeInMinutes;
    }

    public LocalDateTime getLastClientResetAt() {
        return this.lastClientResetAt;
    }

    public final void setLastClientResetAt() {
        this.lastClientResetAt = LocalDateTime.now();
    }
}

