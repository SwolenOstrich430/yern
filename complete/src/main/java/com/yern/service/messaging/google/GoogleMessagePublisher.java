package com.yern.service.messaging.google;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.yern.service.messaging.MessagePublisher;

import reactor.core.publisher.Mono;

@Service
public class GoogleMessagePublisher implements MessagePublisher {

    private PubSubTemplate template;

    public GoogleMessagePublisher(
        @Autowired PubSubTemplate template
    ) {
        this.template = template;
    }

    @Override
    public Mono<String> publishMessage(
        String topicName, 
        String messagePayload
    ) {
        return Mono.fromFuture(
            template.publish(topicName, messagePayload)
        );

    }
}
