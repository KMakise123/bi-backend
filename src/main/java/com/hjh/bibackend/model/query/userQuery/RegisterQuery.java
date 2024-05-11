package com.hjh.bibackend.model.query.userQuery;

import com.hjh.bibackend.common.constant.UserConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class RegisterQuery implements Serializable {

    private static final long serialVersionUID = 9036993548085158662L;

    @NotBlank
    @Length(min = 4,max = 10,message="userAccount的长度必须在4位以上和10位以下")
    @Pattern(regexp = UserConstant.ACCOUNT_REGEX,message="账户格式不正确")
    String account;

    @NotBlank
    @Length(min = 8 ,max = 16,message = "usePassword的长度必须在8为以上和16为以下")
    @Pattern(regexp = UserConstant.PASSWORD_REGEX,message = "编号为数字和字母组合")
    String password;

    @NotBlank
    @Length(min = 8 ,max = 16,message = "usePassword的长度必须在8为以上和16为以下")
    @Pattern(regexp = UserConstant.PASSWORD_REGEX,message = "编号为数字和字母组合")
    String checkPassword;

    @NotBlank
    @Pattern(regexp = UserConstant.PHONE_REGEX,message = "手机号码错误")
    String phone;
}
