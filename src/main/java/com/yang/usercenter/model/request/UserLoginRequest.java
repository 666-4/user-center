package com.yang.usercenter.model.request;
/*
 * Author: 咸余杨
 * */


import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -2869047192196293573L;

    private String userAccount;

    private String userPassword;
}
