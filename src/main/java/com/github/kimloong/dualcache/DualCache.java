package com.github.kimloong.dualcache;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author kimloong
 */
public class DualCache implements Cache {

    private final Cache level1Cache;

    private final Cache level2Cache;

    private final CacheSynchronizer cacheSynchronizer;

    public DualCache(Cache level1Cache, Cache level2Cache, CacheSynchronizer cacheSynchronizer) {
        this.level1Cache = level1Cache;
        this.level2Cache = level2Cache;
        this.cacheSynchronizer = cacheSynchronizer;
    }

    @Override
    public String getName() {
        return level1Cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper valueWrapper = level1Cache.get(key);
        if (null == valueWrapper) {
            valueWrapper = level2Cache.get(key);
            if (null != valueWrapper) {
                level1Cache.put(key, valueWrapper.get());
            }
        }
        return valueWrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        T value = level1Cache.get(key, type);
        if (null == value) {
            value = level2Cache.get(key, type);
            if (null != value) {
                level1Cache.put(key, value);
            }
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        T value = level1Cache.get(key, valueLoader);
        if (null == value) {
            value = level2Cache.get(key, valueLoader);
            if (null != value) {
                level1Cache.put(key, value);
            }
        }
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        level2Cache.put(key, value);
        level1Cache.put(key, value);
        cacheSynchronizer.onPut(getName(), key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        level2Cache.putIfAbsent(key, value);
        ValueWrapper valueWrapper = level1Cache.putIfAbsent(key, value);
        cacheSynchronizer.onPutIfAbsent(getName(), key, value);
        return valueWrapper;
    }

    @Override
    public void evict(Object key) {
        level2Cache.evict(key);
        level1Cache.evict(key);
        cacheSynchronizer.onEvict(getName(), key);
    }

    @Override
    public void clear() {
        level2Cache.clear();
        level1Cache.clear();
        cacheSynchronizer.onClear(getName());
    }
}
