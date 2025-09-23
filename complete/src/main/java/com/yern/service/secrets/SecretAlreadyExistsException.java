package com.yern.service.secrets;

public class SecretAlreadyExistsException extends RuntimeException {
    public SecretAlreadyExistsException(String message) {
        super(message);
    }
}