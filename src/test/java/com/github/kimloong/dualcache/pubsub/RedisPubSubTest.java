package com.github.kimloong.dualcache.pubsub;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RedisPubSubConfig.class)
public class RedisPubSubTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisPubSub() throws InterruptedException {
        ChannelTopic topic = new ChannelTopic("some-topic");

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(stringRedisTemplate.getConnectionFactory());
        container.afterPropertiesSet();
        container.start();
        container.addMessageListener(new SubscribeListener(), topic);

        stringRedisTemplate.convertAndSend(
                topic.getTopic(), "Hello, Redis pub/sub"
        );
        Thread.sleep(1000);
        assertEquals(1, SubscribeListener.COUNTER.get());
    }
}
