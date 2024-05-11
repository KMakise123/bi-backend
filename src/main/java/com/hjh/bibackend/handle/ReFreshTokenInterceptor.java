package com.hjh.bibackend.handle;

import cn.hutool.core.bean.BeanUtil;
import com.hjh.bibackend.common.constant.JwtConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.service.JwtService;
import com.hjh.bibackend.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 只负责刷新token
 * */
@Slf4j
public class ReFreshTokenInterceptor implements HandlerInterceptor {


    private JwtService jwtService;

    private StringRedisTemplate stringRedisTemplate;

    public ReFreshTokenInterceptor(StringRedisTemplate stringRedisTemplate,JwtService jwtService){
        this.jwtService = jwtService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     *请求时拦截
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        log.info("[本次请求URL]: {}",request.getServerName()+":"+ request.getServerPort()+request.getRequestURI());
        log.info("[本次请求方法]: {}",request.getMethod());

        //对跨域验证直接放行
        if(request.getMethod().equals("OPTIONS"))return true;

        String token = request.getHeader("Authorization"); //从请求头中获取JWT access_token
        if(StringUtils.isBlank(token)){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录~");
        }
        try {
            // 解析并验证JWT token是否合法
            boolean isTokenExpired = jwtService.isTokenExpired(token);
            User user = jwtService.validateToken(token);
            if(isTokenExpired){
                // 如果token过期 , 那么需要通过refresh_token生成一个新的access_token
                String refreshTokenKey = JwtConstant.REFRESH_TOKEN_PREFIX+ user.getId();
                String refreshToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
                if(StringUtils.isEmpty(refreshToken)){
                    throw new BusinessException(ErrorCode.NOT_LOGIN,"missing refresh token");
                }
                if(jwtService.isTokenExpired(refreshToken)){
                    throw new BusinessException(ErrorCode.NOT_LOGIN,"超时, 请重新登录");
                }
                // 生成新的accessToken , 同时保存到redis
                String accessToken = jwtService.generateAccessToken(user);
                String accessTokenKey = JwtConstant.ACCESS_TOKEN_PREFIX +user.getId();
                stringRedisTemplate.opsForValue().set(accessTokenKey,accessToken, JwtConstant.EXPIRATION_TIME, TimeUnit.SECONDS);
                response.setHeader("Authorization",accessToken);
                // 更新token这个动作在用户看来是未知的, 更新完之后需要在ThreadLocal中添加UserDTO
                UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
                UserHolder.setUser(userDto);
            }else{
                // 如果token没有过期, 那么直接添加用户的数据
                UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
                UserHolder.setUser(userDto);
            }
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "token错误, 请重新登录");
        }
    }
}

