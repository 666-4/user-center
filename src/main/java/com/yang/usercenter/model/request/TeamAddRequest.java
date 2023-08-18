package com.yang.usercenter.model.request;
/*
 * Author: 咸余杨
 * */


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamAddRequest implements Serializable {

    private static final long serialVersionUID = 260120162614057870L;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 队伍描述
     */
    private String teamDescription;

    /**
     * 组队最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd")
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
