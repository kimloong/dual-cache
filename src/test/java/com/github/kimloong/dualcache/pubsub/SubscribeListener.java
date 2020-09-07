package com.github.kimloong.dualcache.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.concurrent.atomic.AtomicInteger;

public class SubscribeListener implements MessageListener {

    public static AtomicInteger COUNTER = new AtomicInteger();

    /**
     * <h2>消息回调</h2>
     *
     * @param message {@link Message} 消息体 + ChannelName
     * @param pattern 订阅的 pattern, ChannelName 的模式匹配
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        COUNTER.incrementAndGet();
        String body = new String(message.getBody());
        String channel = new String(message.getChannel());
        String patternStr = new String(pattern);

        System.out.println("body:" + body);
        System.out.println("channel:" + channel);
        // 如果是 ChannelTopic, 则 channel 字段与 pattern 字段值相同
        System.out.println("patternStr:" + patternStr);
    }
}
