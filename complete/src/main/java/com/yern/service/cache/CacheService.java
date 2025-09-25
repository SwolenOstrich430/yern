package com.yern.service.cache;

public interface CacheService {
    public boolean hasKey(String key);
    public <V> V get(String key);
    public <V> void set(String key, V val);
    public boolean delete(String key);
}
