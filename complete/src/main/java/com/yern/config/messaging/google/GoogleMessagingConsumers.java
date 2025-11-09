package com.yern.config.messaging.google;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.google.cloud.spring.pubsub.reactive.PubSubReactiveFactory;
import com.yern.dto.pattern.CounterLogCreateRequest;
import com.yern.dto.storage.ProcessFileRequest;
import com.yern.service.messaging.BatchMessageConsumer;
import com.yern.service.messaging.ListProcessor;
import com.yern.service.messaging.google.CustomPubSubMessageConverter;
import com.yern.service.messaging.google.GoogleBatchMessageConsumer;
import com.yern.service.pattern.CounterService;
import com.yern.service.storage.file.FileService;

@Configuration
public class GoogleMessagingConsumers {
    @Bean
    public ThreadPoolTaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(20);
        return  taskScheduler;
    }
    
    @Bean 
    public BatchMessageConsumer counterUpdateConsumer(
        PubSubReactiveFactory factory,
        CounterService counterService,
        CustomPubSubMessageConverter mapper
    ) {
        GoogleBatchMessageConsumer consumer = new GoogleBatchMessageConsumer<CounterLogCreateRequest>();
        consumer.setFactory(factory);
        consumer.setSubscriptionName("counter-update");
        consumer.setMapper(mapper);
        consumer.setBufferSize(50);
        consumer.setPollingMs(1000L);
        consumer.setTargetType(CounterLogCreateRequest.class);
        consumer.setHandler((logs) -> counterService.createLogs(logs));

        consumer.startProcessing();
        return consumer;
    }

    @Bean 
    public BatchMessageConsumer fileUpdateConsumer(
        PubSubReactiveFactory factory,
        FileService fileService,
        CustomPubSubMessageConverter mapper
    ) {
        GoogleBatchMessageConsumer consumer = new GoogleBatchMessageConsumer<ProcessFileRequest>();
        consumer.setFactory(factory);
        consumer.setSubscriptionName("file-update-sub");
        consumer.setMapper(mapper);
        consumer.setBufferSize(50);
        consumer.setPollingMs(1000L);
        consumer.setTargetType(ProcessFileRequest.class);
        ListProcessor<ProcessFileRequest> processor = files -> {
            fileService.processFiles(files);
        };
        consumer.setHandler(processor);
        consumer.startProcessing();
        return consumer;
    }
}