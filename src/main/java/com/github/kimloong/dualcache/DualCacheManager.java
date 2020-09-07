package com.github.kimloong.dualcache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author kimloong
 */
public class DualCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private final CacheManager level1CacheManager;

    private final CacheManager level2CacheManager;

    private CacheSynchronizer cacheSynchronizer;

    public DualCacheManager(CacheManager level1CacheManager,
                            RedisCacheManager level2CacheManager,
                            CacheSynchronizer cacheSynchronizer) {
        this.level1CacheManager = level1CacheManager;
        this.level2CacheManager = level2CacheManager;
        this.cacheSynchronizer = cacheSynchronizer;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = this.cacheMap.get(name);
        if (null == cache) {
            synchronized (this.cacheMap) {
                cache = this.cacheMap.get(name);
                if (cache == null) {
                    cache = this.createCache(name);
                    this.cacheMap.put(name, cache);
                }
            }
        }
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        Collection<String> level1CacheNames = level1CacheManager.getCacheNames();
        Collection<String> level2CacheNames = level2CacheManager.getCacheNames();
        Set<String> cacheNames = new HashSet<>(level1CacheNames.size() + level2CacheNames.size());
        cacheNames.addAll(level1CacheNames);
        cacheNames.addAll(level2CacheNames);
        return cacheNames;
    }

    protected Cache createCache(String name) {
        Cache level1Cache = level1CacheManager.getCache(name);
        Cache level2Cache = level2CacheManager.getCache(name);

        if (null == level1Cache) {
            return level2Cache;
        }
        cacheSynchronizer.onAddCache(level1Cache, level2Cache);

        return new DualCache(level1Cache, level2Cache, cacheSynchronizer);
    }
}
