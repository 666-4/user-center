package com.yang.usercenter.common;
/*
 * Author: 咸余杨
 * */


/**
 * 错误码
 */
public enum ErrorCode {

    SUCCESS(0,"OK",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(4001,"请求数据为空",""),
    NOT_NULL(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    NO_UPDATE(40101,"更新数据错误",""),
    SYSTEM_ERROR(50000,"系统内部异常",""),
    FORBIDDEN(40300,"禁止操作" ,"");

    private final int code;

    private final String message;

    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
