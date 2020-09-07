package com.github.kimloong.dualcache;

import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author kimloong
 */
public class RedisPubSubCacheSynchronizer implements CacheSynchronizer {

    private static final String CHANNEL_TOPIC_NAME_PREFIX = "Dual-Cache~";

    private final RedisTemplate<?, ?> redisTemplate;
    private final RedisMessageListenerContainer messageListenerContainer;
    private final RedisSerializer<?> valueSerializer;

    public RedisPubSubCacheSynchronizer(RedisTemplate<?, ?> redisTemplate) {
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
        ChannelTopic topic = new ChannelTopic(CHANNEL_TOPIC_NAME_PREFIX + level2Cache.getName());
        messageListenerContainer.addMessageListener((message, bytes) -> {
            MessageBody messageBody = deserializeMessageBody(message.getBody());
            switch (messageBody.getOperation()) {
                case Constants.OPERATION_EVICT:
                    level1Cache.evict(messageBody.getKey());
                    break;
                case Constants.OPERATION_PUT:
                    level1Cache.put(messageBody.getKey(), messageBody.getValue());
                    break;
                case Constants.OPERATION_PUT_IF_ABSENT:
                    level1Cache.putIfAbsent(messageBody.getKey(), messageBody.getValue());
                    break;
                case Constants.OPERATION_CLEAR:
                    level1Cache.clear();
                    break;
                default:
            }
        }, topic);
    }

    @Override
    public void onEvict(String cacheName, Object key) {
        MessageBody message = new MessageBody(Constants.OPERATION_EVICT, key);
        redisTemplate.convertAndSend(getTopicName(cacheName), message);
    }

    @Override
    public void onPut(String cacheName, Object key, Object value) {
        MessageBody message = new MessageBody(Constants.OPERATION_PUT, key, value);
        redisTemplate.convertAndSend(getTopicName(cacheName), message);
    }

    @Override
    public void onPutIfAbsent(String cacheName, Object key, Object value) {
        MessageBody message = new MessageBody(Constants.OPERATION_EVICT, key, value);
        redisTemplate.convertAndSend(getTopicName(cacheName), message);
    }

    @Override
    public void onClear(String cacheName) {
        MessageBody message = new MessageBody(Constants.OPERATION_CLEAR);
        redisTemplate.convertAndSend(getTopicName(cacheName), message);
    }

    private String getTopicName(String cacheName) {
        return CHANNEL_TOPIC_NAME_PREFIX + cacheName;
    }

    private MessageBody deserializeMessageBody(byte[] body) {
        return (MessageBody) valueSerializer.deserialize(body);
    }
}
