package com.yang.usercenter.model.vo;
/*
 * Author: 咸余杨
 * */

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -4359588943110351875L;
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
     * 组队最大人数
     */
    private Integer maxNum;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 已加入队伍的人数
     */
    private Integer hasJoinNum;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;

    /**
     * 加入队伍的用户信息
     */
    // todo 关联一哈如队伍的用户信息
    private UserVO userVO;
}
