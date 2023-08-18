package com.yang.usercenter.service;

import com.yang.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/*
 * Author: 咸余杨
 * */

/**
 * 用户服务测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAccount() {
        User user = new User();
        user.setUsername("杨哥哥");
        user.setUserAccount("124");
        user.setAvatarUrl("0000");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setPhone("123");
        user.setEmail("123@qq.com");
        user.setUserStatus(0);
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assert.assertTrue("true",result);
    }

    @Test
    public void userRegister() {
        String userAccount = "1223";
        String userPassword = "";
        String checkPassword = "12345";
        String planetCode = "12334";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assert.assertEquals(-1,result);
        userAccount = "yan";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assert.assertEquals(-1,result);
        userAccount = "1232";
        userPassword = "13456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assert.assertEquals(-1,result);
        userAccount = "yan g";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assert.assertEquals(-1,result);
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assert.assertEquals(-1,result);


    }

    @Test
    public void searchUserByTagsTest() {
        List<String> tagNames = new ArrayList<>();
        tagNames.add("男");
        tagNames.add("大二");
        tagNames.add("大一");
        List<User> userList = userService.searchUserByTags(tagNames);
        System.out.println(userList);
    }

}