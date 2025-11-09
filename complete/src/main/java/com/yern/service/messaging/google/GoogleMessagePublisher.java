package com.yern.service.messaging.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.google.pubsub.v1.PubsubMessage;
import com.yern.service.messaging.MessagePublisher;

import reactor.core.publisher.Mono;

@Service
public class GoogleMessagePublisher implements MessagePublisher {

    private PubSubTemplate template;
    private PubSubMessageConverter mapper;

    public GoogleMessagePublisher(
        @Autowired PubSubTemplate template,
        @Autowired PubSubMessageConverter mapper 
    ) {
        this.template = template;
        this.mapper = mapper;
    }

    @Override
    public Mono<String> publishMessage(
        String topicName,
        Object payload
    ) {
        return publishMessage(
            topicName, 
            mapper.toPubSubMessage(payload, null)
        );
    }

    public Mono<String> publishMessage(
        String topicName, 
        PubsubMessage messagePayload
    ) {
        return Mono.fromFuture(
            template.publish(topicName, messagePayload)
        );

    }
}
