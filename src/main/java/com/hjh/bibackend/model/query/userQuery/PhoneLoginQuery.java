package com.hjh.bibackend.model.query.userQuery;

import com.hjh.bibackend.common.constant.UserConstant;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class PhoneLoginQuery implements Serializable {

    private static final long serialVersionUID = -5418765270397590191L;

    @NotBlank
    @Pattern(regexp = UserConstant.PHONE_REGEX,message = "手机号码错误")
    private String phone;

    @NotBlank
    @Pattern(regexp = UserConstant.PHONE_CODE_REGEX,message = "验证码不正确")
    private String code;
}
