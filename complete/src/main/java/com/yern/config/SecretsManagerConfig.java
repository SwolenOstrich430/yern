package com.yern.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

@Configuration
@Component
public class SecretsManagerConfig {
    
    public SecretsManagerConfig() {}

    @Bean 
    public SecretManagerServiceClient googleSecretsManagerClient() throws IOException {
        return SecretManagerServiceClient.create();
    }
}
