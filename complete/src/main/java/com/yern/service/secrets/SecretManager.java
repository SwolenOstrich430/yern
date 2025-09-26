package com.yern.service.secrets;

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
    public SecretImpl getSecret(String secretName, Optional<String> version) throws SecretNotFoundException;

    /**
     * Creates a new secret not attached to any other, existing version.
     *
     * @param secretName The full name of the secret.
     * @param secret The actual value to be stored under the `secretName`
     * @throws SecretAlreadyExistsException If that secret already exists. 
    */
    public void createSecret(String secretName, String secret) throws SecretAlreadyExistsException;

    /**
     * Removes a secret based on the provided name.
     *
     * @param secretName The full name of the secret.
     * @throws RuntimeException If the secret cannot be deleted.
    */
    public void deleteSecret(String secretName);
    
      /**
     * Removes a secret based on the provided name.
     *
     * @param secretName The full name of the secret.
     * @param version The version of the secret. If the value is empty, the latest version will be used.
     * @throws RuntimeException If the secret cannot be deleted.
    */
    public void deleteVersion(String secretName, Optional<String> version);
    
    /**
     * Updates the supplied secret to a state that is no longer considered valid for production use.
     * 
     * @param secretName The full name of the secret.
     * @param version The version of the secret. If the value is empty, the latest version will be used.
     * @throws RuntimeException If the secret cannot be found.
    */
    public void disableVersion(String secretName, Optional<String> version);

    /**
     * Updates the supplied secret to a state that is valid for production use.
     * 
     * @param secretName The full name of the secret.
     * @param version The version of the secret. If the value is empty, the latest version will be used.
     * @throws RuntimeException If the secret cannot be found.
    */
    public void enableVersion(String secretName, Optional<String> version);
}
