package com.hjh.bibackend.model.vo.user;

import com.hjh.bibackend.common.constant.UserConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class UserAddVo implements Serializable {

    private static final long serialVersionUID = -3053086144584995115L;

    /**
     * 账户
     * */
    @NotBlank
    @Length(min = 4,max = 10,message="account的长度必须在4位以上和10位以下")
    @Pattern(regexp = UserConstant.ACCOUNT_REGEX,message="账户格式不正确")
    private String account;

    @NotBlank
    @Length(min = 8,max = 16,message = "password长度必须在8位以上和16位以下")
    @Pattern(regexp = UserConstant.PASSWORD_REGEX,message = "密码格式不正确")
    private String password;

    /**
     * 用户名
     * */
    private String username;

    /**
     * 头像Url
     * */
    private String avatarUrl;

    /**
     * 手机号码
     * */
    @NotBlank
    @Pattern(regexp = UserConstant.PHONE_REGEX,message = "手机号码格式错误")
    private String phone;

    /**
     * 邮件
     * */
    @NotBlank
    private String email;

    /**
     * 用户角色
     */
    private String userRole;
}
