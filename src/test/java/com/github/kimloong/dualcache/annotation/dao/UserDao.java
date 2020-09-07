package com.github.kimloong.dualcache.annotation.dao;

import com.github.kimloong.dualcache.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@CacheConfig(cacheManager = "userCacheManager")
public class UserDao {

    public static final AtomicInteger COUNTER = new AtomicInteger();

    @Cacheable(cacheNames = "user", key = "#p0.toString()")
    public User findOne(Long id) {
        System.out.println("===User.findOne invocation===");
        COUNTER.getAndIncrement();
        User user = new User();
        user.setId(id);
        user.setFirstName("firstName" + id);
        user.setLastName("lastName" + id);
        user.setNickName("nickName" + RandomStringUtils.randomAlphanumeric(6));
        user.setAge(ThreadLocalRandom.current().nextInt(100));
        return user;
    }

    @CacheEvict(cacheNames = "user", key = "#p0.toString()")
    public void delete(Long id) {
        System.out.println("===User.delete invocation===");
    }
}
