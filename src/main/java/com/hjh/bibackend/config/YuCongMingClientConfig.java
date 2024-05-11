package com.hjh.bibackend.config;

import com.yupi.yucongming.dev.client.YuCongMingClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yuapi.client")
public class YuCongMingClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public YuCongMingClient yuCongMingClient(){
        return new YuCongMingClient(accessKey,secretKey);
    }
}
