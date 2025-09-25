package com.yern.service.secrets;

import com.yern.service.secrets.SecretNotFoundException;
import com.yern.service.secrets.SecretAlreadyExistsException;

import java.io.IOException;
import java.util.Optional;

/**
 * Defines the subset of methods that all cloud secret providers should implement.
 */
public interface SecretManager {
    /**
     * Searches for a secret matching the `secretName` and `version` params.
     * If that secret is not found, a SecretNotFoundException is thrown.
     * If the secret is found, it's returned from the method.
     *
     * @param secretName The full name of the secret.
     * @param version The version of the secret. If the value is empty, the latest version will be returned.
     * @throws SecretNotFoundException If that secret is not found, a SecretNotFoundException is thrown.
    */
    public String get(String secretName, Optional<String> version) throws IOException, SecretNotFoundException;

    /**
     * Creates a new secret not attached to any other, existing version.
     *
     * @param secretName The full name of the secret.
     * @param secret The actual value to be stored under the `secretName`
     * @throws SecretAlreadyExistsException If that secret already exists. 
    */
    public void create(String secretName, String secret) throws IOException, SecretAlreadyExistsException;

    /**
     * Complete removes a secret based on the provided name.
     *
     * @param secretName The full name of the secret.
     * @param version The version of the secret. If the value is empty, the latest version will be used.
     * @throws RuntimeException If the secret cannot be deleted.
    */
    public void delete(String secretName, Optional<String> version) throws IOException, RuntimeException;
    
    /**
     * Updates the supplied secret to a state that is no longer considered valid for production use.
     * 
     * @param secretName The full name of the secret.
     * @throws RuntimeException If the secret cannot be found.
    */
    public void disable(String secretName) throws IOException, RuntimeException;
}
