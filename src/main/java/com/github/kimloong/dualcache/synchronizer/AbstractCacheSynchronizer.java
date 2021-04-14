package com.github.kimloong.dualcache.synchronizer;

import com.github.kimloong.dualcache.Message;
import com.github.kimloong.dualcache.Operations;
import org.springframework.cache.Cache;

import java.util.Objects;

public abstract class AbstractCacheSynchronizer implements CacheSynchronizer {


    private final String group;

    private SynchronizerInterceptor interceptor = new DefaultSynchronizerInterceptor();

    public AbstractCacheSynchronizer() {
        this("");
    }

    public AbstractCacheSynchronizer(String group) {
        this.group = group;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public SynchronizerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(SynchronizerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public abstract void onAddCache(Cache level1Cache, Cache level2Cache);

    @Override
    public void publishEvictEvent(String cacheName, Object key) {
        Message message = new Message(group, Operations.EVICT, key);
        message = interceptor.transformToPublishMessage(group, cacheName, message);
        publishEvent(cacheName, message);
    }

    @Override
    public void publishPutEvent(String cacheName, Object key, Object value) {
        Message message = new Message(group, Operations.PUT, key, value);
        message = interceptor.transformToPublishMessage(group, cacheName, message);
        publishEvent(cacheName, message);
    }

    @Override
    public void publishPutIfAbsentEvent(String cacheName, Object key, Object value) {
        Message message = new Message(group, Operations.EVICT, key, value);
        message = interceptor.transformToPublishMessage(group, cacheName, message);
        publishEvent(cacheName, message);
    }

    @Override
    public void publishClearEvent(String cacheName) {
        Message message = new Message(group, Operations.CLEAR);
        message = interceptor.transformToPublishMessage(group, cacheName, message);
        publishEvent(cacheName, message);
    }

    @Override
    public void doReceiveEvent(Cache level1Cache, Cache level2Cache, Message message) {
        if (null == message) {
            return;
        }
        message = interceptor.transformFromReceivedMessage(group, level1Cache.getName(), message);
        switch (message.getOperation()) {
            case Operations.EVICT:
                level1Cache.evict(message.getKey());
                if (isDifferentGroup(message.getGroup())) {
                    level2Cache.evict(message.getKey());
                }
                break;
            case Operations.PUT:
                level1Cache.put(message.getKey(), message.getValue());
                if (isDifferentGroup(message.getGroup())) {
                    level2Cache.put(message.getKey(), message.getValue());
                }
                break;
            case Operations.PUT_IF_ABSENT:
                level1Cache.putIfAbsent(message.getKey(), message.getValue());
                if (isDifferentGroup(message.getGroup())) {
                    level2Cache.putIfAbsent(message.getKey(), message.getValue());
                }
                break;
            case Operations.CLEAR:
                level1Cache.clear();
                if (isDifferentGroup(message.getGroup())) {
                    level2Cache.clear();
                }
                break;
            default:
                //EMPTY
        }
    }

    protected String getSourceCacheName(String cacheName) {
        return interceptor.getSourceCacheName(group, cacheName);
    }

    protected abstract void publishEvent(String cacheName, Message message);

    private boolean isDifferentGroup(String messageGroup) {
        return Objects.equals(messageGroup, group);
    }
}
