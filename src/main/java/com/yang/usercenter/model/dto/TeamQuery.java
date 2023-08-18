package com.yang.usercenter.model.dto;
/*
 * Author: 咸余杨
 * */


import com.yang.usercenter.model.mo.PageDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TeamQuery extends PageDto {

    /**
     * 主键
     */
    private Long id;

    /**
     * List 主键ids
     */
    private List<Long> idList;

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 搜索关键词（同时对队伍名称和队伍描述进行搜索）
     */
    private String searchText;

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
     * 队伍状态
     */
    private Integer teamStatus;



}
