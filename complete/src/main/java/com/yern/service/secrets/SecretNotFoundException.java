package com.yern.service.secrets;

public class SecretNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Unable to find secret: ";

    public static String getFormattedMessage(String secretName) {
        return (DEFAULT_MESSAGE + secretName);
    }

    public SecretNotFoundException(String secretName) {
        super(getFormattedMessage(secretName));
    }
}
