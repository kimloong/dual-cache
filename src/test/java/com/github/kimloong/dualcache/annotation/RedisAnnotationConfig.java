package com.github.kimloong.dualcache.annotation;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kimloong.dualcache.DualCacheManager;
import com.github.kimloong.dualcache.synchronizer.RedisPubSubCacheSynchronizer;
import com.google.common.cache.CacheBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class RedisAnnotationConfig {

    public static final int LOCAL_CACHE_DURATION_MILLISECONDS = 3000;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("127.0.0.1");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setDefaultSerializer(genericJackson2JsonRedisSerializer());
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setHashKeySerializer(redisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Primary
    @Bean
    public GuavaCacheManager localCacheManager() {
        GuavaCacheManager level1CacheManager = new GuavaCacheManager();
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .expireAfterWrite(LOCAL_CACHE_DURATION_MILLISECONDS, TimeUnit.MILLISECONDS)
                .maximumSize(10000);
        level1CacheManager.setCacheBuilder(cacheBuilder);
        return level1CacheManager;
    }

    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate());
        cacheManager.setDefaultExpiration(3600);
        cacheManager.setUsePrefix(true);
        // 这里可进行一些配置 包括默认过期时间 每个cacheName的过期时间等 前缀生成等等
        return cacheManager;
    }

    @Bean
    public DualCacheManager userCacheManager() {
        GuavaCacheManager level1CacheManager = localCacheManager();
        RedisPubSubCacheSynchronizer redisPubSubCacheSynchronizer = new RedisPubSubCacheSynchronizer(redisTemplate());
        return new DualCacheManager(level1CacheManager, redisCacheManager(), redisPubSubCacheSynchronizer);
    }
}
