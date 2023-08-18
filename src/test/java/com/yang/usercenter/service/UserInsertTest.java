package com.yang.usercenter.service;
/*
 * Author: 咸余杨
 * */

import cn.hutool.core.date.StopWatch;
import com.yang.usercenter.mapper.UserMapper;
import com.yang.usercenter.model.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量插入数据
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserInsertTest {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    /**
     * 单线程批量插入
     */
    @Test
    public void userInsertTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("yang哥哥");
            user.setUserAccount("04200112");
            user.setAvatarUrl("https://picx.zhimg.com/v2-3e6ebc244cbc1b4271c51b1e44b138db_1440w.jpg?source=172ae18b");
            user.setUserPassword("123");
            user.setPhone("12231");
            user.setEmail("123@qq.com");
            user.setPlanetCode("231");
            user.setTags("[]");
            user.setProfile("你好啊！");
            userList.add(user);
        }
        userService.saveBatch(userList,5000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 多线程批量插入
     */
    @Test
    public void duoThreadUserInsertTest() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("yang哥哥");
            user.setUserAccount("04200112");
            user.setAvatarUrl("https://picx.zhimg.com/v2-3e6ebc244cbc1b4271c51b1e44b138db_1440w.jpg?source=172ae18b");
            user.setUserPassword("123");
            user.setPhone("12231");
            user.setEmail("123@qq.com");
            user.setPlanetCode("231");
            user.setTags("[]");
            user.setProfile("你好啊！");
            userList.add(user);
        }
        userService.saveBatch(userList,5000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
