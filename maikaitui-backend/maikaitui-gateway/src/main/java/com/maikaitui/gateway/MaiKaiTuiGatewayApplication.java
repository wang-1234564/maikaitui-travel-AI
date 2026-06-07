package com.maikaitui.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MaiKaiTuiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaiKaiTuiGatewayApplication.class, args);
    }
}
