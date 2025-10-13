package com.yern.service.messaging.google;

import java.time.Duration;
import java.util.List;

import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.yern.service.messaging.BatchMessageConsumer;
import com.yern.service.messaging.ListProcessor;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Getter 
@Setter
public class GoogleBatchMessageConsumer<T> implements BatchMessageConsumer<AcknowledgeablePubsubMessage, T> {
    private PubSubReactiveFactory factory;
    private String subscriptionName;
    private CustomPubSubMessageConverter mapper;
    private int bufferSize;
    private Long pollingMs;
    private Class<T> targetType;
    private ListProcessor<T> handler;

    public GoogleBatchMessageConsumer(
        PubSubReactiveFactory factory,
        String subscriptionName,
        CustomPubSubMessageConverter mapper,
        int bufferSize,
        Long pollingMs,
        Class<T> targetType,
        ListProcessor<T> handler
    ) {
        this.factory = factory;
        this.subscriptionName = subscriptionName;
        this.mapper = mapper;
        this.bufferSize = bufferSize;
        this.pollingMs = pollingMs;
        this.targetType = targetType;
        this.handler = handler;
    }

    public GoogleBatchMessageConsumer() {}

    public void startProcessing() {
        factory.poll(subscriptionName, pollingMs) 
                .take(bufferSize)
                .buffer(Duration.ofMillis(500L)) 
                .flatMap(this::processBatch)
                .doOnError(this::onError)
                .subscribe();
    }

    // TODO: only ack the messages that have been converted successfully 
    @Override
    public Mono<Void> processBatch(
        List<AcknowledgeablePubsubMessage> messages
    ) {
        return Mono.fromRunnable(() -> {
            List<T> convertedMessages = (List<T>) messages.stream().map(
                message -> mapper.from(
                    message.getPubsubMessage(),
                    targetType
                )
            ).toList();

            handler.process(convertedMessages);
            messages.stream().forEach(message -> message.ack());
        });
    }

    @Override
    public void onError(Throwable err) {        
        return;
        // throw new Exception(err);
    }

    public void setHandler(ListProcessor<T> handler2) {
        this.handler = handler2;
    }
}