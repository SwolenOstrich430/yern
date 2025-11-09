package com.yern.service.messaging;

import reactor.core.publisher.Mono;

public interface MessagePublisher {
    // TODO: add type safety to this/make it generic??
    public Mono<String> publishMessage(String topicName, Object messagePayload);
}