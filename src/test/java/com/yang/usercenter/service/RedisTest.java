package com.yang.usercenter.service;
/*
 * Author: 咸余杨
 * */


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest()
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void RedisSetValueTest() {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("yangInt",113);
        valueOperations.set("yangString","憨憨");
        valueOperations.set("yangDouble",0.34);
    }

    @Test
    public void RedisGetValueTest() {
        System.out.println(redisTemplate.opsForValue().get("yangInt"));
        System.out.println(redisTemplate.opsForValue().get("yangString"));
        System.out.println(redisTemplate.opsForValue().get("yangDouble"));
    }

    @Test
    public void RedisDeleteValueTest() {
        System.out.println(redisTemplate.delete("yangInt"));
        System.out.println(redisTemplate.delete("yangString"));
        System.out.println(redisTemplate.delete("yangDouble"));
    }
}
