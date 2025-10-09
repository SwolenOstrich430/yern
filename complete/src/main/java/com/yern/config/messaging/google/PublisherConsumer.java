package com.yern.config.messaging.google;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import com.google.cloud.spring.pubsub.support.converter.PubSubMessageConverter;
import com.yern.dto.pattern.SectionCounterLogCreateRequest;
import com.yern.model.pattern.SectionCounterLog;
import com.yern.service.messaging.google.GoogleBatchMessageConsumer;
import com.yern.service.pattern.SectionCounterService;

@Configuration
public class PublisherConsumer {
    
    @Bean 
    public GoogleBatchMessageConsumer googleBatchMessageConsumer(
        PubSubReactiveFactory factory,
        SectionCounterService sectionService,
        PubSubMessageConverter mapper
    ) {
        GoogleBatchMessageConsumer consumer = new GoogleBatchMessageConsumer<SectionCounterLog>();
        consumer.setFactory(factory);
        consumer.setSubscriptionName("counter-update");
        consumer.setMapper(mapper);
        consumer.setBufferSize(50);
        consumer.setPollingMs(1000L);
        consumer.setTargetType(SectionCounterLogCreateRequest.class);
        consumer.setHandler((logs) -> sectionService.createLogs(logs));

        consumer.startProcessing();
        return consumer;
    }
}