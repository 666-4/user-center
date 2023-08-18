package com.yang.usercenter.model.enums;
/*
 * Author: 咸余杨
 * */

/**
 * 队伍状态枚举
 */
public enum TeamStatusEnum {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");


    /**
     * 获取队伍的状态信息
     * @param value
     * @return
     */
    public static TeamStatusEnum getEnumByTagValue(Integer value) {
        if(value == null) {
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if(value.equals(teamStatusEnum.getValue())) {
                return teamStatusEnum;
            }
         }
        return null;
    }


    private int value;

    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
