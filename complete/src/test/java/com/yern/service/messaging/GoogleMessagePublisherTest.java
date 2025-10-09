package com.yern.service.messaging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.yern.service.messaging.google.GoogleMessagePublisher;

import io.netty.util.concurrent.Future;

import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoogleMessagePublisherTest {
    private GoogleMessagePublisher publisher;
    private PubSubTemplate template; 
    private final String topicName = UUID.randomUUID().toString();
    private final String messagePayload = UUID.randomUUID().toString();
    private CompletableFuture<String> future;
    
    @BeforeEach 
    public void setup() {
        this.template = mock(PubSubTemplate.class);
        this.publisher = new GoogleMessagePublisher(template);
        this.future = mock(CompletableFuture.class);
    }

    @Test 
    public void publishMessage_actsAsWrapper_forPubSubTemplate() {
        when(template.publish(topicName, messagePayload)).thenReturn(future);
        publisher.publishMessage(topicName, messagePayload);
        verify(template.publish(topicName, messagePayload));
    }
}
