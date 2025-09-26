package com.yern.service.secrets;

public class SecretAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Secret already exists: ";

    public static String getFormattedMessage(String secretName) {
        return (DEFAULT_MESSAGE + secretName);
    }
    
    public SecretAlreadyExistsException(String message) {
        super(message);
    }
}