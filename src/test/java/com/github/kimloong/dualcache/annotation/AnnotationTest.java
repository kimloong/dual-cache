package com.github.kimloong.dualcache.annotation;

import com.github.kimloong.dualcache.annotation.dao.UserDao;
import com.github.kimloong.dualcache.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RedisAnnotationConfig.class)
public class AnnotationTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserDao userDao;

    @Test
    public void shouldGetNullFromRedisTemplate() {
        int oldCounter = UserDao.COUNTER.get();
        long userId = 1L;
        redisTemplate.opsForValue().set("user:" + userId, null);
        Object value = redisTemplate.opsForValue().get("user:" + userId);
        Boolean aBoolean = redisTemplate.hasKey("user:" + userId);
        System.out.println(aBoolean);
        System.out.println(value);
        User user = userDao.findOne(userId);
        int newCounter = UserDao.COUNTER.get();
        assertEquals(oldCounter, newCounter);
    }

    @Test
    public void shouldSendEvictMessageOnEvict(){
        long userId = 1L;
        User user = userDao.findOne(userId);
        userDao.delete(userId);
    }
}
