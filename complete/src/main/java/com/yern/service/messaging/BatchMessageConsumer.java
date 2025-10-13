package com.yern.service.messaging;

import java.util.List;

import reactor.core.publisher.Mono;

public interface BatchMessageConsumer<T, L> {
    String getSubscriptionName();
    void startProcessing();
    Mono<Void> processBatch(List<T> messages);
    // public Mono<Void> processBatch(List<T> messages);
    void onError(Throwable err) throws Exception;
}
