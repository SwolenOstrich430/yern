package com.yern.service.secrets.gcp;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import com.yern.service.secrets.SecretAlreadyExistsException;
import com.yern.service.secrets.SecretManager;
import com.yern.service.secrets.SecretNotFoundException;

import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;

/**
 * Implementation of the SecretsManager interface for Google. Idea is to be able to switch between
 * different secret managers easily if we move to a different cloud provider.
 * TODO: look into https://github.com/GoogleCloudPlatform/spring-cloud-gcp/blob/main/spring-cloud-gcp-secretmanager/src/main/java/com/google/cloud/spring/secretmanager/SecretManagerTemplate.java
 */
@Service
public class GoogleSecretManager implements SecretManager {
    private SecretManagerServiceClient client;
    private final Duration maxClientLifeInMinutes;
    private LocalDateTime lastClientResetAt;
    // TODO: this should be returned dynamically in another story 
    private final String projectId;
    private final String latestVersionAlias;

    public GoogleSecretManager(
        @Value("${secrets.google.client.max-life-in-minutes}") 
        Duration maxClientLifeInMinutes,
        @Value("${secrets.google.latest-version-alias}")
        String latestVersionAlias,
        @Autowired 
        SecretManagerServiceClient client
    ) throws IOException {
        this.maxClientLifeInMinutes = maxClientLifeInMinutes;
        // TODO: move this into a util method 
        this.projectId = new DefaultGcpProjectIdProvider().getProjectId();
        this.latestVersionAlias = latestVersionAlias;
        this.setClient(client);
    }

    /**
     * Spring should now be managing this? 
     */
    public final void setClient() throws IOException {
        if ((this.client == null) || this.isClientExpired()) {
            this.setLastClientResetAt();
            this.client = this.createClient();
        }
    }

    public final void setClient(SecretManagerServiceClient newClient) {
        this.client = newClient;
        this.setLastClientResetAt();
    }

    public SecretManagerServiceClient getClient() {
        return client;
    }

    public String get(
        String secretName, 
        Optional<String> version
     ) throws IOException, SecretNotFoundException {
        setClient();

        AccessSecretVersionRequest req = getSecretAccessRequest(
            secretName, version
        );
        AccessSecretVersionResponse resp = client.accessSecretVersion(req);

        return parseAccessSecretResponse(resp, secretName); 
    }

    public void create(String secretName, String secret) throws IOException, SecretAlreadyExistsException {
        setClient();

        Secret baseSecret = this.getBaseSecretTemplate();
        Secret createdSecret = this.client.createSecret(
            this.projectId, secretName, baseSecret
        );

        SecretPayload payload = getCreateSecretPayload(secret);

        this.client.addSecretVersion(
            createdSecret.getName(), 
            payload
        );
    }

    public void delete(String secretName, Optional<String> version) throws RuntimeException {

    }

    public void disable(String secretName) throws RuntimeException {

    }

    // TODO: figure out how serializing fits into this
    public String parseAccessSecretResponse(
        AccessSecretVersionResponse response,
        String secretName
    ) {
        if (!response.hasPayload()) {
            throw new SecretNotFoundException(secretName);
        }

        return response.getPayload().getData().toString();
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

    public AccessSecretVersionRequest getSecretAccessRequest(
        String secretName,
        Optional<String> version
    ) {
        SecretVersionName formattedVersion = 
            SecretVersionName.ofProjectSecretSecretVersionName(
                this.projectId,
                secretName,
                version.orElse(this.latestVersionAlias)
            );
        
        return AccessSecretVersionRequest
                    .newBuilder()
                    .setName(formattedVersion.toString())
                    .build();

    }

    public SecretManagerServiceClient createClient() throws IOException {
        if (this.client != null) {
            this.closeClient();
        }

        return SecretManagerServiceClient.create();
    }

    public void closeClient() {
        if (client == null) {
            return;
        }

        try {
            if (!client.awaitTermination(2, TimeUnit.SECONDS)) {
                client.close();
            }
        } catch (Exception exception) {
            client.close(); 
        }
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

        // TODO: check if >= exists?
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

