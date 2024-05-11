package com.hjh.bibackend.config;

import com.hjh.bibackend.handle.DefaultWebSocketHandler;
import com.hjh.bibackend.Interceptor.WebSocketInterceptor;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Resource
    public DefaultWebSocketHandler defaultWebSocketHandler;

    @Resource
    public WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(defaultWebSocketHandler, "ws/message")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
