package com.hjh.bibackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAuthVo implements Serializable {
    private static final long serialVersionUID = 974546435686450633L;

    private String token;

    private String userInfo;
}
