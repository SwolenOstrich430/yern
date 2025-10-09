package com.yern.service.messaging.google;

import java.util.List;
import java.util.function.Function;

import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.yern.service.messaging.BatchMessageConsumer;
import com.yern.service.messaging.ListProcessor;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Getter 
@Setter
public class GoogleBatchMessageConsumer<T> implements BatchMessageConsumer<AcknowledgeablePubsubMessage, T> {
    private PubSubReactiveFactory factory;
    private String subscriptionName;
    private PubSubMessageConverter mapper;
    private int bufferSize;
    private Long pollingMs;
    private Class<T> targetType;
    private ListProcessor<T> handler;

    public GoogleBatchMessageConsumer(
        PubSubReactiveFactory factory,
        String subscriptionName,
        PubSubMessageConverter mapper,
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

    public GoogleBatchMessageConsumer() {
        //TODO Auto-generated constructor stub
    }

    public void startProcessing() {
        factory.poll(subscriptionName, pollingMs) 
                .buffer(bufferSize) 
                .flatMap(this::processBatch)
                .doOnError(t -> {
                    try {
                        onError(t);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                })
                .subscribe();
    }

    // TODO: only ack the messages that have been converted successfully 
    @Override
    public Mono<Void> processBatch(
        List<AcknowledgeablePubsubMessage> messages
    ) {
        return Mono.fromRunnable(() -> {
            List<T> convertedMessages = messages.stream().map(
                message -> mapper.fromPubSubMessage(
                    message.getPubsubMessage(),
                    targetType
                )
            ).toList();

            handler.process(convertedMessages);
            messages.stream().forEach(message -> message.ack());
        });
    }

    @Override
    public void onError(Throwable err) throws Exception {
        throw new Exception(err);
    }

    public void setHandler(ListProcessor<T> handler2) {
        this.handler = handler2;
    }
}