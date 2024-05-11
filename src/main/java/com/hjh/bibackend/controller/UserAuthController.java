package com.hjh.bibackend.controller;

import com.hjh.bibackend.common.BaseResponse;
import com.hjh.bibackend.common.constant.UserConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.model.query.userQuery.AccountLoginQuery;
import com.hjh.bibackend.model.query.userQuery.PhoneLoginQuery;
import com.hjh.bibackend.model.query.userQuery.RegisterQuery;
import com.hjh.bibackend.model.vo.UserAuthVo;
import com.hjh.bibackend.service.LoginService;
import com.hjh.bibackend.utils.ResponseUtils;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@RestController
@ResponseBody
public class UserAuthController {

    @Resource
    LoginService loginService;

    //发送验证码
    @PostMapping("/send/phone/code")
    public BaseResponse<String> sendPhoneVerifyCode(
            @NotBlank
            @Pattern(regexp = UserConstant.PHONE_REGEX,message = "手机号格式不正确")
            @RequestParam String phone){
        String code = loginService.createPhoneCode(phone);
        return ResponseUtils.success(code);
    }

    //手机号登录
    @PostMapping("/phone/login")
    public BaseResponse<UserAuthVo> phoneLogin(@RequestBody @Validated PhoneLoginQuery phoneLoginQuery){
        String phone = phoneLoginQuery.getPhone();
        String code = phoneLoginQuery.getCode();
        UserAuthVo userAuthVo = loginService.phoneLogin(phone,code);
        return ResponseUtils.success(userAuthVo);
    }

    //账号密码登录
    @PostMapping("/account/login")
    public BaseResponse<UserAuthVo> accountLogin(@RequestBody @Validated AccountLoginQuery accountLoginQuery){
        String userAccount = accountLoginQuery.getUserAccount();
        String userPassword = accountLoginQuery.getUserPassword();
        UserAuthVo userAuthVo = loginService.accountLogin(userAccount,userPassword);
        return ResponseUtils.success(userAuthVo);
    }

    //注册
    @PostMapping("/register")
    public BaseResponse<UserAuthVo> register(@RequestBody @Validated RegisterQuery registerQuery){
        String userAccount = registerQuery.getAccount();
        String userPassword = registerQuery.getPassword();
        String userCheckPassword = registerQuery.getCheckPassword();
        String phone = registerQuery.getPhone();
        User user = loginService.doRegister(userAccount,userPassword,userCheckPassword,phone);
        if(user==null)throw new BusinessException(ErrorCode.PARAMS_ERROR);
        UserAuthVo userAuthVo = loginService.accountLogin(user.getAccount(),userPassword);
        return ResponseUtils.success(userAuthVo);
    }
}
