package com.yang.usercenter.once;
import java.util.Date;
/*
 * Author: 咸余杨
 * */

import cn.hutool.core.date.StopWatch;
import com.yang.usercenter.mapper.UserMapper;
import com.yang.usercenter.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 批量插入数据
 */
@Component
public class InsertUser {


    @Resource
    private UserMapper userMapper;

//    @Scheduled(fixedDelay = 5000)
    public void insertManyUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
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
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
