package com.yang.usercenter.job;
/*
 * Author: 咸余杨
 * */


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yang.usercenter.constant.UserConstant;
import com.yang.usercenter.model.domain.User;
import com.yang.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务，缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    // TODO 从数据库中查询要使用频繁的用户Id
    // 先缓存一些测试用户
    private List<Long> mainUserId = Arrays.asList(1L,2L,3L,4L,5L);

    @Resource
    private RedissonClient redissonClient;

//    @Scheduled(cron = "0 0 1 ? * L")
    public void doCacheReconnectUser() {
        RLock lock = redissonClient.getLock(UserConstant.YANG_USER_REDISSON_LOCK);
        try {
            if(lock.tryLock(0,30000,TimeUnit.MILLISECONDS)){
                for (Long userId : mainUserId) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = new Page<>(1,10);
                    Page<User> page = userService.page(userPage, queryWrapper);
                    List<User> records = page.getRecords();
                    try {
                        redisTemplate.opsForValue().set(UserConstant.YANG_USER_RECOMMEND + userId,records,1800, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 判断是否是当前线程持有的锁，如果是才释放锁
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
