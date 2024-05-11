package com.hjh.bibackend.Interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hjh.bibackend.common.ErrorCode;
import com.hjh.bibackend.common.constant.JwtConstant;
import com.hjh.bibackend.exception.BusinessException;
import com.hjh.bibackend.model.domain.User;
import com.hjh.bibackend.model.dto.UserDto;
import com.hjh.bibackend.service.JwtService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Configuration
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Resource
    private JwtService jwtService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes
    ) throws Exception {
        //对跨域验证直接放行
        if(request.getMethod().equals("OPTIONS"))return true;

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            ServletServerHttpResponse servletServerHttpResponse = (ServletServerHttpResponse) response;

            //String userId = servletServerHttpRequest.getServletRequest().getParameter("uid");
            String token = servletServerHttpRequest.getServletRequest().getParameter("Authorization");
            if(StringUtils.isBlank(token)){
                throw new BusinessException(ErrorCode.NOT_LOGIN,"请先登录~");
            }
            try {
                // 解析并验证JWT token是否合法
                boolean isTokenExpired = jwtService.isTokenExpired(token);
                User user = jwtService.validateToken(token);
                if(isTokenExpired) {
                    // 如果token过期 , 那么需要通过refresh_token生成一个新的access_token
                    String refreshTokenKey = JwtConstant.REFRESH_TOKEN_PREFIX + user.getId();
                    String refreshToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
                    if (StringUtils.isEmpty(refreshToken)) {
                        throw new BusinessException(ErrorCode.NOT_LOGIN, "missing refresh token");
                    }
                    if (jwtService.isTokenExpired(refreshToken)) {
                        throw new BusinessException(ErrorCode.NOT_LOGIN, "超时, 请重新登录");
                    }

                }else{
                    // 更新token这个动作在用户看来是未知的, 更新完之后需要在ThreadLocal中添加UserDTO
                    UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);
                    attributes.put("user",userDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }



    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("握手结束");
    }
}
