package com.hjh.bibackend.utils;


import com.hjh.bibackend.common.BaseResponse;
import com.hjh.bibackend.common.ErrorCode;

public class ResponseUtils {

    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(2000,data,"The operation was successful",null);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error(ErrorCode errorCode){
        return  new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode,String msg,String description){
        return new BaseResponse<>(errorCode.getCode(),null,msg,description);
    }

    public static BaseResponse error(ErrorCode errorCode,String description){
        return new BaseResponse<>(errorCode.getCode(),null,errorCode.getMessage(),description);
    }

    public static BaseResponse error(int code,String message,String description){
        return new BaseResponse<>(code,null,message,description);
    }
}
