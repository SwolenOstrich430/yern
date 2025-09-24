package com.yern.service.secrets;

public class SecretAlreadyExistException extends RuntimeException {
    public SecretAlreadyExistException(String message) {
        super(message);
    }
}