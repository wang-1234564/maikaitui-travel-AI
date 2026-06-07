package com.maikaitui.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 迈开腿 - 系统管理服务启动类
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.maikaitui.system", "com.maikaitui.common"})
@MapperScan("com.maikaitui.system.mapper")
public class MaiKaiTuiSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaiKaiTuiSystemApplication.class, args);
    }
}
