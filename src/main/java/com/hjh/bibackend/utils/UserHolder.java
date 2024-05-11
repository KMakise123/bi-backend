package com.hjh.bibackend.utils;


import com.hjh.bibackend.model.dto.UserDto;

/**
 *ThreadLocal线程，保存用户的脱敏信息
 */
public class UserHolder {
    public static ThreadLocal<UserDto> userDtoThreadLocal = new ThreadLocal<>();

    public static UserDto getUser() {
        return userDtoThreadLocal.get();
    }

    public static void setUser(UserDto userDto) {
        userDtoThreadLocal.set(userDto);
    }
}
