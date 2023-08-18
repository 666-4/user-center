package com.yang.usercenter.model.request;
/*
 * Author: 咸余杨
 * 用户注册请求体
 * */


import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {


    private static final long serialVersionUID = 5979290527775761180L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;


    private String planetCode;
}
