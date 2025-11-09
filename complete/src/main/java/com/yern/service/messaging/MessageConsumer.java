package com.yern.service.messaging;

import reactor.core.publisher.Mono;

public interface MessageConsumer<T, L> {
    String getSubscriptionName();
    void startProcessing();
    void onError(Throwable err);
	Mono<Void> process(T message);
}