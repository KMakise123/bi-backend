package com.hjh.bibackend.config;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GsonConfig {
    @Bean
    public Gson createGson(){
        return new Gson();
    }
}
