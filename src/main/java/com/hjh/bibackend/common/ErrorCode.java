package com.hjh.bibackend.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAMS_ERROR(40000,"请求参数错误","111"),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    SYSTEM_ERROR(50000,"服务器内部错误",""),
    NOT_FOUND(40400,"找不到对应的资源",""),
    ERROR_ROLE(40800,"身份不一致",""),
    ERROR_STATUS(40900,"状态异常",""),
    NOT_WAITING_REVIEW(41000,"未处于待审核状态",""),
    OPERATION_ERROR(50001, "操作失败",""),
    FORBIDDEN_ERROR(40300, "禁止访问",""),
    TOO_MANY_REQUEST(42900, "请求过于频繁","");

    private final int code;

    private final String message;

    private final String description;

    ErrorCode(int code, String message, String description){
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
