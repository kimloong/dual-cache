package com.github.kimloong.dualcache.annotation.controller;

import com.github.kimloong.dualcache.annotation.dao.UserDao;
import com.github.kimloong.dualcache.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/{user_id}")
    public User get(@PathVariable("user_id") Long id) {
        return userDao.findOne(id);
    }

    @DeleteMapping("/{user_id}")
    public void delete(@PathVariable("user_id") Long id) {
        userDao.delete(id);
    }
}
