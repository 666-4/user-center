package com.yang.usercenter.model.request;
/*
 * Author: 咸余杨
 * */


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 1309611102524087614L;
    /**
     * 主键
     */

    private Long teamId;

    /**
     * 密码
     */
    private String teamPassword;



}
