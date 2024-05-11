package com.hjh.bibackend.aop;

import com.hjh.bibackend.annotation.RoleCheck;
import com.hjh.bibackend.common.constant.UserConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class RoleCheckAop {
    /**
     * 增强方法，将其绑定在注解RoleCheck上
     * 1.先获取当前用户的角色
     * 2.获取注解上的mustRole的值
     * 3.通过mustRole与当前用户角色进行比对。若校验通过，则放行。否则，将异常抛出由全局异常处理器处理。
     * */
    @Around("@annotation(roleCheck)")
    public Object run(ProceedingJoinPoint joinPoint, RoleCheck roleCheck) throws Throwable{
       String mustRole = roleCheck.mustRole();
       if(StringUtils.isNotBlank(mustRole)){
           UserDto userDto = UserHolder.getUser();
           if(userDto==null){
               throw new BusinessException(ErrorCode.NOT_LOGIN);
           }
           String userRole = userDto.getUserRole();
           Integer userStatus = userDto.getStatus();
           //检查封号，不用担心userStatus被篡改
           if(UserConstant.ALLOW_USER != userStatus){
               throw new BusinessException(ErrorCode.NO_AUTH);
           }
           //需要管理员权限
           if(!UserConstant.ADMIN.equals(userRole)){
               throw new BusinessException(ErrorCode.NO_AUTH);
           }
       }
       //放行
       return joinPoint.proceed();
    }
}
