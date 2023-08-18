package com.yang.usercenter.utils;
/*
 * Author: 咸余杨
 * */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class RedisCacheUtil {

    @Resource
    private StringRedisTemplate redisTemplate;

    // 定义一个序列化对象
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 设置没有过期时间的缓存对象没有过期时间
     * @param key
     * @param object
     * @throws JsonProcessingException
     */
    public void set(String key,Object object) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(object);
        redisTemplate.opsForValue().set(key,json);
    }

    /**
     * 设置有过期时间的缓存
     * @param key
     * @param object
     * @param timeout
     * @param unit
     * @throws JsonProcessingException
     */
    public void setExpiration(String key, Object object, long timeout, TimeUnit unit) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(object);
        redisTemplate.opsForValue().set(key,json,timeout,unit);
    }

    public void get(String key,Object object){
        String s = redisTemplate.opsForValue().get(key);
        Class<?> aClass = object.getClass();
    }
}
