package com.github.kimloong.dualcache;

import com.github.kimloong.dualcache.synchronizer.CacheSynchronizer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <pre>
 * 为适配复杂场景的缓存，提供本地+分布式两级缓存, 两者不一定都必须存在
 * 1. 有本地缓存，有分布式缓存，适合缓存量小，但待缓存对象生成(获取)成本高的场景
 * 2. 有本地缓存，无分布式缓存，适合缓存量小
 * 3. 无本地缓存，有分布式缓存，可以直接去使用RedisCacheManager，但例外的是如果使用不同的分布式缓存实例(集群)，则可以借助他来实现同步
 * 4. 无本地缓存，无分布式缓存，要这种场景干嘛
 * </pre>
 * 无缓存不要使用null来进行表示，请使用{code org.springframework.cache.support.NoOpCacheManager}
 *
 * @author kimloong
 */
public class DualCacheManager implements CacheManager {

    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);

    private final CacheManager level1CacheManager;

    private final CacheManager level2CacheManager;

    private final CacheSynchronizer cacheSynchronizer;

    public DualCacheManager(CacheManager level1CacheManager,
                            CacheManager level2CacheManager,
                            CacheSynchronizer cacheSynchronizer) {
        Assert.notNull(level1CacheManager, "level1CacheManager not null," +
                "please use org.springframework.cache.support.NoOpCacheManager");
        Assert.notNull(level1CacheManager, "level2CacheManager not null," +
                "please use org.springframework.cache.support.NoOpCacheManager");
        Assert.notNull(cacheSynchronizer, "cacheSynchronizer not null");
        this.level1CacheManager = level1CacheManager;
        this.level2CacheManager = level2CacheManager;
        this.cacheSynchronizer = cacheSynchronizer;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = cacheMap.get(name);
        if (null == cache) {
            synchronized (cacheMap) {
                cache = cacheMap.get(name);
                if (null == cache) {
                    cache = createCache(name);
                    cacheMap.put(name, cache);
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

        cacheSynchronizer.onAddCache(level1Cache, level2Cache);

        return new DualCache(level1Cache, level2Cache, cacheSynchronizer);
    }
}
