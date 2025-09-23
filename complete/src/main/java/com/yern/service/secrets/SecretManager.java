package com.yern.service.secrets;

import java.util.List;
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
     * @param version The version of the secret. If the value is empty, the latest version will be returned
     * @throws SecretNotFoundException If that secret is not found, a SecretNotFoundException is thrown.
    */
    public String get(String secretName, Optional<String> version) throws SecretNotFoundException;
}
