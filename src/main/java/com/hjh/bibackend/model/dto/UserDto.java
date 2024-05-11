package com.hjh.bibackend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {
    private static final long serialVersionUID = -7800498109048888720L;

    /**
     * 用户ID 主键
     */
    private Long id;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 用户状态 0-正常 1-不正常
     */
    private String username;

    /**
     * 用户状态 0-正常 1-不正常
     */
    private Integer status;

    /**
     * 用户角色
     */
    private String userRole;

}
