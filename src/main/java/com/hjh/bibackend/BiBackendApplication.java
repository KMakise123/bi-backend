package com.hjh.bibackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.hjh.bibackend.mapper")
public class BiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiBackendApplication.class, args);
    }

}
