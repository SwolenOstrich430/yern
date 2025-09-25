package com.yern.config.cache.redis;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.redis.cache.RedisCacheWriter.TtlFunction;

import java.time.Duration;

// TODO: add actual logic for each entry
enum TtlFunctionImpl implements TtlFunction {
    INSTANCE;

    @Override
    @NonNull
    public Duration getTimeToLive(@NonNull Object key, @Nullable Object value) {
        // setting this to 15 minutes for now
        return Duration.ofMinutes(15);
    }
}
