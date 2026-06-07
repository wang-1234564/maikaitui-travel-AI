package com.maikaitui.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.maikaitui.ai", "com.maikaitui.common"})
@MapperScan("com.maikaitui.ai.mapper")
public class MaiKaiTuiAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaiKaiTuiAiApplication.class, args);
    }
}
