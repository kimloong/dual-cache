package com.github.kimloong.dualcache.annotation;

import com.github.kimloong.dualcache.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RedisAnnotationConfig.class)
public class MultipleInstanceAnnotationTest {

    private static final Long USER_ID = 1L;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final RestTemplate restTemplate = new RestTemplate();

    private static SpringApplicationBuilder instance1;

    private static SpringApplicationBuilder instance2;


    @Before
    public void before() {
        if (null == instance1) {
            instance1 = new SpringApplicationBuilder(UserWebApplication.class)
                    .properties("server.port=8081");
            instance1.run();
        }
        if (null == instance2) {
            instance2 = new SpringApplicationBuilder(UserWebApplication.class)
                    .properties("server.port=8082");
            instance2.run();
        }
    }

    @After
    public void after() {
        clear();
    }

    @Test
    public void shouldReadLevel2CacheOnLocalCacheMiss() throws Exception {
        String url1 = "http://127.0.0.1:8081/users/" + USER_ID;
        String url2 = "http://127.0.0.1:8082/users/" + USER_ID;
        User userFromInstance1 = restTemplate.getForObject(url1, User.class);
        User userFromInstance2 = restTemplate.getForObject(url2, User.class);
        assertEquals(userFromInstance1.getNickName(), userFromInstance2.getNickName());
    }

    @Test
    public void shouldSyncOnEvictCache() throws Exception {
        String url1 = "http://127.0.0.1:8081/users/" + USER_ID;
        String url2 = "http://127.0.0.1:8082/users/" + USER_ID;
        User userFromInstance1 = restTemplate.getForObject(url1, User.class);
        User userFromInstance2 = restTemplate.getForObject(url2, User.class);

        restTemplate.delete(url1);

        User userFromInstance1SecondRead = restTemplate.getForObject(url1, User.class);
        User userFromInstance2SecondRead = restTemplate.getForObject(url2, User.class);

        assertEquals(userFromInstance1.getNickName(), userFromInstance2.getNickName());
        assertNotEquals(userFromInstance1.getNickName(), userFromInstance1SecondRead.getNickName());
        assertEquals(userFromInstance1SecondRead.getNickName(), userFromInstance2SecondRead.getNickName());
    }

    private void clear() {
        // wait local cache expire
        try {
            Thread.sleep(RedisAnnotationConfig.LOCAL_CACHE_DURATION_MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.delete("user:" + USER_ID);
    }
}
