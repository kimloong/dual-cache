package com.github.kimloong.dualcache.synchronizer;

import com.github.kimloong.dualcache.Message;

public interface SynchronizerInterceptor {

    Message transformToPublishMessage(String group, String cacheName, Message message);

    Message transformFromReceivedMessage(String group, String cacheName, Message message);

    String getSourceCacheName(String group, String cacheName);
}
