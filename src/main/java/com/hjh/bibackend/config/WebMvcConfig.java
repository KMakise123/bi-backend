package com.hjh.bibackend.config;

import com.hjh.bibackend.handle.ReFreshTokenInterceptor;
import com.hjh.bibackend.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    JwtService jwtService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                //设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //跨域允许时间
                .maxAge(3600)
                .allowedHeaders("*")
                .exposedHeaders("*");
        WebMvcConfigurer.super.addCorsMappings(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ReFreshTokenInterceptor(stringRedisTemplate,jwtService)).addPathPatterns("/**").excludePathPatterns(
                "/**/account/login",
                "/**/phone/login",
                "/**/send/phone/code",
                "/**/register",
                "/**/logout",
                "/**/doc.html",
                "ws/message"
        );
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
