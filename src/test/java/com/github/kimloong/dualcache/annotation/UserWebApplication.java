package com.github.kimloong.dualcache.annotation;

import com.github.kimloong.dualcache.annotation.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserWebApplication {

    @Autowired
    private UserDao userDao;
}
