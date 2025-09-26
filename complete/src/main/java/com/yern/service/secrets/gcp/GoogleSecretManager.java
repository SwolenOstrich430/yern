package com.yern.service.secrets.gcp;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.SecretVersion.State;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.protobuf.ByteString;
import com.yern.service.secrets.SecretImpl;
import com.yern.service.secrets.SecretManager;
import com.yern.service.secrets.SecretNotFoundException;

/**
 * Implementation of the SecretsManager interface for Google. Idea is to be able to switch between
 * different secret managers easily if we move to a different cloud provider.
 */
@Service
public class GoogleSecretManager implements SecretManager {
    private SecretManagerServiceClient client;
    private final String projectId;
    // TODO: move to util method 
    private final String fullProjectId;
    private final String latestVersionAlias;

    public GoogleSecretManager(
        @Value("${secrets.google.client.max-life-in-minutes}") 
        Duration maxClientLifeInMinutes,
        @Value("${secrets.google.latest-version-alias}")
        String latestVersionAlias,
        @Autowired 
        SecretManagerServiceClient client
    ) {
        // TODO: move this into a util method/class  
        this.projectId = new DefaultGcpProjectIdProvider().getProjectId();
        this.fullProjectId = "projects/" + this.projectId;
        this.latestVersionAlias = latestVersionAlias;
        this.client = client;
    }

    public SecretManagerServiceClient getClient() {
        return client;
    }

    public SecretImpl getSecret(
        String secretName, 
        Optional<String> version
     ) throws SecretNotFoundException {
        AccessSecretVersionRequest req = getSecretAccessRequest(
            secretName, version
        );

        try {
            AccessSecretVersionResponse resp = client.accessSecretVersion(req);
            return parseAccessSecretResponse(resp, secretName); 
        } catch(NotFoundException e) {
            throw new SecretNotFoundException(secretName);
        }
    }

    public void createSecret(String secretName, String secret) {
        Secret baseSecret = this.getBaseSecretTemplate();

        try {
            this.client.createSecret(this.fullProjectId, secretName, baseSecret);
        } catch (AlreadyExistsException e) {}
        
        this.addVersion(secretName, secret);
    }

    public void deleteSecret(String secretName) {
        client.deleteSecret(
            getDeleteSecretRequest(secretName)
        );
    }

      // TODO: add unit tests
    public void addVersion(String secretName, String secret) {
        this.client.addSecretVersion(
            SecretName.of(projectId, secretName), 
            getCreateSecretPayload(secret)
        );
    }

    public void deleteVersion(String secretName, Optional<String> version) throws RuntimeException {
        SecretVersionName secretVersionName = SecretVersionName.of(
            this.projectId, secretName, version.orElse(this.latestVersionAlias)
        );

        SecretVersion disabledVersion = client.destroySecretVersion(secretVersionName);
        assert(disabledVersion.getState() == State.DESTROYED);
    }

    public void disableVersion(String secretName, Optional<String> version) throws RuntimeException {
        SecretVersionName secretVersionName = SecretVersionName.of(
            this.projectId, secretName, version.orElse(this.latestVersionAlias)
        );

        SecretVersion disabledVersion = client.disableSecretVersion(secretVersionName);
        assert(disabledVersion.getState() == State.DISABLED);
    }

    public void enableVersion(String secretName, Optional<String> version) throws RuntimeException {
        SecretVersionName secretVersionName = SecretVersionName.of(
            this.projectId, secretName, version.orElse(this.latestVersionAlias)
        );

        SecretVersion disabledVersion = client.enableSecretVersion(secretVersionName);
        assert(disabledVersion.getState() == State.ENABLED);
    }

    // TODO: figure out how serializing fits into this
    public SecretImpl parseAccessSecretResponse(
        AccessSecretVersionResponse response,
        String secretName
    ) {
        if (!response.hasPayload()) {
            throw new SecretNotFoundException(secretName);
        }

        String[] versionSegments = response.getName().split("/");

        return new SecretImpl(
            secretName, 
            response.getPayload().getData().toStringUtf8(), 
            versionSegments[versionSegments.length - 1],
            response.getName()
        );
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

    public DeleteSecretRequest getDeleteSecretRequest(String secretName) {
        return DeleteSecretRequest.newBuilder()
            .setName(SecretName.of(projectId, secretName).toString())
            .build();
    }
}

