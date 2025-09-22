package com.yern.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {
    @Autowired
    private RedisTemplate<String, Object> template;

    @Override
    public boolean hasKey(String key) {
        return template.hasKey(key);
    }

    @Override
    public <V> V get(String key) {
        return (V) template.opsForValue().get(key); 
    }

    @Override
    public <V> void set(String key, V val) {
        template.opsForValue().set(key, val);
    }

    @Override 
    public boolean delete(String key) {
        return template.delete(key);
    }
}
