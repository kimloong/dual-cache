package com.github.kimloong.dualcache.synchronizer;

import com.github.kimloong.dualcache.Message;

public class DefaultSynchronizerInterceptor implements SynchronizerInterceptor {

    @Override
    public Message transformToPublishMessage(String group, String cacheName, Message message) {
        return message;
    }

    @Override
    public Message transformFromReceivedMessage(String group, String cacheName, Message message) {
        return message;
    }

    @Override
    public String getSourceCacheName(String group, String cacheName) {
        return cacheName;
    }
}
