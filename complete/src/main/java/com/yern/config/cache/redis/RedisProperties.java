package com.yern.config.cache.redis;

import java.time.Duration;
import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.Getter;

@Getter
@Data
@Validated
@ConfigurationProperties(prefix = "redis.config")
@EnableConfigurationProperties(RedisProperties.class)
public class RedisProperties {
    private final Duration readTimeout;
    private final List<String> nodes;
    private final int maxTotalPool;
    private final int maxIdlePool;
    private final int minIdlePool;
    private final Duration maxWait;
    private final int maxRedirects;

    public RedisProperties(
            Duration readTimeout,
            List<String> nodes,
            int maxTotalPool,
            int maxIdlePool,
            int minIdlePool,
            Duration maxWait,
            int maxRedirects
    ) {
        this.readTimeout = readTimeout;
        this.nodes = nodes;
        this.maxTotalPool = maxTotalPool;
        this.maxIdlePool = maxIdlePool;
        this.minIdlePool = minIdlePool;
        this.maxWait = maxWait;
        this.maxRedirects = maxRedirects;
    }

    public <T> GenericObjectPoolConfig<T> getPoolConfig() {
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxTotalPool);
        config.setMaxIdle(maxIdlePool);
        config.setMinIdle(minIdlePool);
        config.setMaxWait(maxWait);
        return config;
    }

    public Duration getReadTimeout() {
        return this.readTimeout;
    }

    public List<String> getNodes() {
        return this.nodes;
    }
}