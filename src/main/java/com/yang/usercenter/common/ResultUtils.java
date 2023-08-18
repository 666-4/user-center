package com.yang.usercenter.common;
/*
 * Author: 咸余杨
 * */

/**
 * 返回工具类
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok","");
    }

    /**
     * 失败
     * @param errorCode
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(int code,String message,String description) {
        return new BaseResponse<>(code,null,message,description);
    }

    public static BaseResponse error(ErrorCode errorcode,String message,String description) {
        return new BaseResponse<>(errorcode.getCode(),null,message,description);
    }

    public static BaseResponse error(ErrorCode errorcode,String description) {
        return new BaseResponse<>(errorcode.getCode(),errorcode.getMessage(),description);
    }

}
