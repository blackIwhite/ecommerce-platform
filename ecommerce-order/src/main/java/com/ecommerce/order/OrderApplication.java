package com.ecommerce.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce")
@MapperScan("com.ecommerce.order.mapper")
@ComponentScan(basePackages = {"com.ecommerce.order", "com.ecommerce.common.web.filter", "com.ecommerce.common.web.config", "com.ecommerce.common.web.interceptor"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
