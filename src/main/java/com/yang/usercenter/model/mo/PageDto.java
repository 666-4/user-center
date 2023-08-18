package com.yang.usercenter.model.mo;
/*
 * Author: 咸余杨
 * */

import lombok.Data;

import java.io.Serializable;

@Data
public class PageDto implements Serializable {

    private static final long serialVersionUID = -8173709490606881757L;
    /**
     * 当前页大小
     */
    public int pageSize = 10;

    /**
     * 当前页
     */
    public int pageNum = 1;

}

