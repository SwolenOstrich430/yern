package com.yern.service.messaging;

import java.util.List;

import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;

import reactor.core.publisher.Mono;

public interface BatchMessageConsumer<T, L> {
    String getSubscriptionName();
    // public Mono<Void> processBatch(List<T> messages);
    void onError(Throwable err) throws Exception;
	Mono<Void> processBatch(List<AcknowledgeablePubsubMessage> messages);
}
