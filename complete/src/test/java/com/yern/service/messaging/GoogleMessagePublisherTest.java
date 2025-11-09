package com.yern.service.messaging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.PubsubMessage;
import com.yern.service.messaging.google.GoogleMessagePublisher;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class GoogleMessagePublisherTest {
    private GoogleMessagePublisher publisher;

    private GoogleMessagePublisher spy;

    @Mock 
    private PubSubMessageConverter messageConverter;

    @Mock 
    private PubSubTemplate template; 

    @Mock 
    private PubsubMessage message;

    @Mock 
    private Mono<String> mono;

    @Mock  
    private CompletableFuture<String> future;

    private final String topicName = UUID.randomUUID().toString();
    private final Object payload = mock(Object.class);
    
    @BeforeEach 
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.publisher = new GoogleMessagePublisher(template, messageConverter);
        this.spy = spy(publisher);
    }

    @Test 
    public void publishMessage_actsAsWrapper_forPubSubTemplate() {        
        try (MockedStatic<Mono> mockedStatic = Mockito.mockStatic(Mono.class)) {
            when(template.publish(
                topicName, message
            )).thenReturn(future);

            mockedStatic.when(() -> Mono.fromFuture(any(CompletableFuture.class))).thenReturn(mono);
            Mono<String> mono1 = publisher.publishMessage(topicName, message);
            assertEquals(mono, mono1);
        }
        
        verify(
            template, times(1)
        ).publish(topicName, message);
    } 

    @Test 
    public void publishMessage_convertsTheProvidedObject_toPubsubMessage() {
        when(messageConverter.toPubSubMessage(payload, null)).thenReturn(message);
        doReturn(mono).when(spy).publishMessage(topicName, message);

        Mono<String> createdFut = spy.publishMessage(topicName, payload);

        assertEquals(mono, createdFut);
    }
}
