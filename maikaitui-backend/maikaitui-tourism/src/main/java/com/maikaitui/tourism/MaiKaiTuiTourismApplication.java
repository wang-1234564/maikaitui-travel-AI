package com.maikaitui.tourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 迈开腿 - 旅游服务启动类
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.maikaitui.tourism", "com.maikaitui.common"})
@MapperScan({"com.maikaitui.tourism.mapper","com.maikaitui.system.mapper"})
public class MaiKaiTuiTourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaiKaiTuiTourismApplication.class, args);
    }
}
