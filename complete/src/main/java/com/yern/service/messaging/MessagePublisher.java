package com.yern.service.messaging;

import reactor.core.publisher.Mono;

public interface MessagePublisher {
    public Mono<String> publishMessage(String topicName, String messagePayload);
}
