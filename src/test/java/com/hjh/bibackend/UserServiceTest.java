package com.hjh.bibackend;
import java.util.Date;

import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void addUser(){
        User user = new User();
        user.setAccount("admin123");
        user.setPassword("123456789");
        user.setUsername("hjh");
        user.setPhone("13211160568");
        user.setEmail("2566684909@qq.com");
        user.setStatus(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        userService.save(user);
    }

    @Test
    public void updateTime(){
        Date date = new Date();
        System.out.println(date);
    }
}
