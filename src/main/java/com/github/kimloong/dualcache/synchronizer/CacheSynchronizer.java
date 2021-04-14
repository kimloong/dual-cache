package com.github.kimloong.dualcache.synchronizer;

import com.github.kimloong.dualcache.Message;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Cache Synchronizer
 * synchronize local cache between different instance
 *
 * @author kimloong
 */
public interface CacheSynchronizer {

    String getGroup();

    /**
     * Invoke this when add cache
     *
     * @param level1Cache the first level cache
     * @param level2Cache the second level cache
     */
    void onAddCache(Cache level1Cache, Cache level2Cache);

    /**
     * Invoke this when evict the mapping for this key from this cache if it is present.
     *
     * @param cacheName the cache name
     * @param key       the key whose mapping is to be removed from the cache
     */
    void publishEvictEvent(String cacheName, Object key);

    /**
     * Invoke this when associate the specified value with the specified key in this cache.
     * <p>If the cache previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     *
     * @param cacheName the cache name
     * @param key       the key with which the specified value is to be associated
     * @param value     the value to be associated with the specified key
     */
    void publishPutEvent(String cacheName, Object key, Object value);

    /**
     * Invoke this when atomically associate the specified value with the specified key in this cache
     * if it is not set already.
     * <p>This is equivalent to:
     * <pre><code>
     * Object existingValue = cache.get(key);
     * if (existingValue == null) {
     *     cache.put(key, value);
     *     return null;
     * } else {
     *     return existingValue;
     * }
     * </code></pre>
     * except that the action is performed atomically. While all out-of-the-box
     * {@link CacheManager} implementations are able to perform the put atomically,
     * the operation may also be implemented in two steps, e.g. with a check for
     * presence and a subsequent put, in a non-atomic way. Check the documentation
     * of the native cache implementation that you are using for more details.
     *
     * @param cacheName the cache name
     * @param key       the key with which the specified value is to be associated
     * @param value     the value to be associated with the specified key
     */
    void publishPutIfAbsentEvent(String cacheName, Object key, Object value);

    /**
     * Invoke this when remove all mappings from the cache.
     *
     * @param cacheName the cache name
     */
    void publishClearEvent(String cacheName);

    void doReceiveEvent(Cache level1Cache, Cache level2Cache, Message message);
}
