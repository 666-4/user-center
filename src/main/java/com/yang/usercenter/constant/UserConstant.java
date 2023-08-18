package com.yang.usercenter.constant;
/*
 * Author: 咸余杨
 * */


public interface UserConstant {

    String USER_LOGIN_STATE = "userLoginStatus";

    // 用户权限常量
    // 管理员权限常量
    int ADMIN_ROLE = 1;

    // 普通用户常量
    int DEFAULT_ROLE = 0;

    String YANG_USER_RECOMMEND = "yang:user:recommend:";

    // redisson 的锁
    String  YANG_USER_REDISSON_LOCK = "yang:user:redisson:lock";

    String  YANG_USER_JOIN_LOCK = "yang:team:join:lock";
}
