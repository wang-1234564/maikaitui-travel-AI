package com.maikaitui.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.maikaitui.auth.mapper")
@ComponentScan(basePackages = {"com.maikaitui.auth", "com.maikaitui.common"})
public class MaiKaiTuiAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaiKaiTuiAuthApplication.class, args);
    }
}
