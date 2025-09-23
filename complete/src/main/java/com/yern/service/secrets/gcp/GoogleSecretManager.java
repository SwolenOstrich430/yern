package com.yern.service.secrets.gcp;

import com.yern.service.secrets.SecretNotFoundException;
import com.yern.service.secrets.SecretAlreadyExistsException;
import com.yern.service.secrets.SecretManager;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Implementation of the SecretsManager interface for Google. Idea is to be able to switch between
 * different secret managers easily if we move to a different cloud provider.
 */
public class GoogleSecretManager implements SecretManager {
    private SecretManagerServiceClient client;
    private Duration maxClientLifeInMinutes;
    private LocalDateTime lastClientResetAt;

    public GoogleSecretManager() {
        this.setLastClientResetAt();
    }
    
    public GoogleSecretManager(
        @Value("${secrets.google.client.max-life-in-minutes}") 
        Duration maxClientLifeInMinutes
    ) {
        this.maxClientLifeInMinutes = maxClientLifeInMinutes;
        this.setLastClientResetAt();
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
    }

    public SecretManagerServiceClient getClient() {
        return client;
    }

    public String get(String secretName, Optional<String> version) throws SecretNotFoundException {
        return "";
    }

    public void create(String secretName, String secret) throws SecretAlreadyExistsException {

    }

    public void delete(String secretName, Optional<String> version) throws RuntimeException {

    }

    public void disable(String secretName) throws RuntimeException {

    }

    public SecretManagerServiceClient createClient() throws IOException {
        if (this.client != null) {
            this.client.close();
        }

        return SecretManagerServiceClient.create();
    }

    public boolean isClientExpired() {
        LocalDateTime currTime = LocalDateTime.now();
        LocalDateTime expiry = this.lastClientResetAt.plus(
            this.maxClientLifeInMinutes
        );
        
        return (
            this.client.isShutdown() || 
            currTime.isAfter(expiry) || 
            currTime.isEqual(expiry)
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
