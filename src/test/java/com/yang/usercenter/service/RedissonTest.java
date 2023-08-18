package com.yang.usercenter.service;
/*
 * Author: 咸余杨
 * */


import com.yang.usercenter.config.RedisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void TestRedisson(){

        RList<String> list = redissonClient.getList("test-redisson");
        list.add("yang");
        System.out.println("list: "+list.get(0));
        list.remove("yang");

        Map<String,Integer> map = redissonClient.getMap("map-test");
        map.put("yang",10);
        map.remove("yang");
        System.out.println(map.get("yang"));
    }
}
