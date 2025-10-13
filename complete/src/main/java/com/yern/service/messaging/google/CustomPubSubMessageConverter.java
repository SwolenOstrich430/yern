package com.yern.service.messaging.google;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import com.google.pubsub.v1.PubsubMessage;
import com.yern.dto.messaging.MessagePayload;

@Primary
@Component
public class CustomPubSubMessageConverter extends JacksonPubSubMessageConverter {

    public CustomPubSubMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public <T> MessagePayload from(
        PubsubMessage message, 
        Class<T> targetType
    ) {
        T convertedResourse = super.fromPubSubMessage(
            message, 
            targetType
        );
        
        MessagePayload payload = (MessagePayload) convertedResourse;
        payload.setExternalId(message.getMessageId());

        return payload;
    }
}
