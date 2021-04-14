package com.github.kimloong.dualcache.synchronizer;

import com.github.kimloong.dualcache.Message;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author kimloong
 */
public class RedisPubSubCacheSynchronizer extends AbstractCacheSynchronizer {

    private static final String CHANNEL_TOPIC_NAME_PREFIX = "Dual-Cache~";

    private final RedisTemplate<?, ?> redisTemplate;
    private final RedisMessageListenerContainer messageListenerContainer;
    private final RedisSerializer<?> valueSerializer;

    public RedisPubSubCacheSynchronizer(RedisTemplate<?, ?> redisTemplate) {
        this("", redisTemplate);
    }

    public RedisPubSubCacheSynchronizer(String group, RedisTemplate<?, ?> redisTemplate) {
        super(group);
        this.redisTemplate = redisTemplate;
        valueSerializer = redisTemplate.getValueSerializer();

        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        messageListenerContainer = new RedisMessageListenerContainer();
        messageListenerContainer.setConnectionFactory(connectionFactory);
        messageListenerContainer.afterPropertiesSet();
        messageListenerContainer.start();
    }

    @Override
    public void onAddCache(Cache level1Cache, Cache level2Cache) {
        String sourceCacheName = getSourceCacheName(level2Cache.getName());
        ChannelTopic topic = new ChannelTopic(CHANNEL_TOPIC_NAME_PREFIX + sourceCacheName);
        messageListenerContainer.addMessageListener((message, bytes) -> {
            Message messageBody = deserializeMessageBody(message.getBody());
            doReceiveEvent(level1Cache, level2Cache, messageBody);
        }, topic);
    }

    @Override
    public void publishEvent(String cacheName, Message message) {
        redisTemplate.convertAndSend(getTopicName(cacheName), message);
    }

    private String getTopicName(String cacheName) {
        return CHANNEL_TOPIC_NAME_PREFIX + cacheName;
    }

    private Message deserializeMessageBody(byte[] body) {
        return (Message) valueSerializer.deserialize(body);
    }
}
