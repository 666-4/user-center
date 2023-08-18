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
public class TeamUpdateRequest implements Serializable {


    private static final long serialVersionUID = -8365599035332461455L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDescription;


    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开 1 - 私有 2 -加密
     */
    private Integer teamStatus;

    /**
     * 密码
     */
    private String teamPassword;



}
