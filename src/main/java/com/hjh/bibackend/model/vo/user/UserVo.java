package com.hjh.bibackend.model.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;
import static com.hjh.bibackend.common.constant.UserConstant.*;


@Data
public class UserVo implements Serializable {

    private static final long serialVersionUID = -8051742191298442458L;

    /**
     * 用户Id
     * */
    @NotNull
    private Long id;

    /**
     * 账户
     * */
    @NotBlank
    @Length(min = 4,max = 10,message="userAccount的长度必须在4位以上和10位以下")
    @Pattern(regexp = ACCOUNT_REGEX,message="账户格式不正确")
    private String account;

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
    @Pattern(regexp = PHONE_REGEX,message = "手机号码格式错误")
    private String phone;

    /**
     * 邮件
     * */
    private String email;

    /**
     * 硬币数量
     */
    private Long coins;

    /**
     * 用户状态 0-正常 1-不正常
     */
    private Integer status;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;
}
