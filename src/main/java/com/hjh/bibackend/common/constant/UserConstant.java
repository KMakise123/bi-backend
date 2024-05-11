package com.hjh.bibackend.common.constant;


public class UserConstant {
    //密码加盐
    public static final String PASSWORD_SALT = "hjhSalt";

    //无效的UserId
    public static final long INVALID_USER_ID = -1;

    //账户正则
    public static final String ACCOUNT_REGEX = "^[a-zA-Z0-9_]*$";

    //最新电话正则
    public static final String PHONE_REGEX = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    //密码正则
    public static final String PASSWORD_REGEX = "^\\s*|[0-9A-Za-z]*$";

    //验证码前缀
    public static final String PHONE_CODE_PREFIX = "phone_code:";

    //验证码正则表达式,只有6位数字组成
    public static final String PHONE_CODE_REGEX = "^\\d{6}$";

    //权限角色
    public static final String ADMIN = "admin";
    public static final String COMMON_USER = "commonUser";

    //用户状态
    public static final int ALLOW_USER = 0;
    public static final int BAN_USER = 1;

    //默认密码
    public static final String DEFAULT_PASSWORD = "123456789";
}
