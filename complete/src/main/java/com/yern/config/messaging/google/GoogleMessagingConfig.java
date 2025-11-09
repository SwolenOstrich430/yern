package com.yern.config.messaging.google;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;


@Configuration
public class GoogleMessagingConfig {

    @Bean
    public PubSubMessageConverter pubSubMessageConverter() {
        return new JacksonPubSubMessageConverter(new ObjectMapper());
    }
}
