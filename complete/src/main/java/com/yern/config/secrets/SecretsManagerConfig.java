package com.yern.config.secrets;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

@Configuration
public class SecretsManagerConfig {

    @Bean
    public SecretManagerServiceClient secretManagerServiceClient() throws IOException {
        return SecretManagerServiceClient.create();
    }
}
