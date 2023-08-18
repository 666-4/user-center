package com.yang.usercenter.model.request;
/*
 * Author: 咸余杨
 * */


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -7539755396678150616L;
    /**
     * 队伍Id
     */
    private Long teamId;

}
