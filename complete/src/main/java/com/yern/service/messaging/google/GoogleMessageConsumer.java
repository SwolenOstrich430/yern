package com.yern.service.messaging.google;

import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import com.google.cloud.spring.pubsub.support.AcknowledgeablePubsubMessage;
import com.yern.service.messaging.MessageConsumer;
import com.yern.service.messaging.Processor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Getter
@Setter
@NoArgsConstructor
public class GoogleMessageConsumer<T> implements MessageConsumer<AcknowledgeablePubsubMessage, T> {

    private PubSubReactiveFactory factory;
    private String subscriptionName; 
    private CustomPubSubMessageConverter mapper;
    private Long pollingMs;
    private Class<T> targetType;
    private Processor<T> handler;

    @Override
    public void startProcessing() {
        factory.poll(subscriptionName, pollingMs) 
                .limitRate(1)
                .doOnNext(this::process) 
                .subscribe();
    }

    @Override
    public Mono<Void> process(AcknowledgeablePubsubMessage message) {
        return Mono.fromRunnable(() -> {
            T convertedMessage = (T) mapper.from(
                message.getPubsubMessage(),
                targetType
            );

            handler.process(convertedMessage);
            message.ack();
        });
    }

    @Override
    public void onError(Throwable err) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    public void setHandler(Processor<T> handler) {
        this.handler = handler;
    }
}
