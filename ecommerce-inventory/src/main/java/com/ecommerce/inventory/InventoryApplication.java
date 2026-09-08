package com.ecommerce.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce")
@MapperScan("com.ecommerce.inventory.mapper")
@ComponentScan(basePackages = {"com.ecommerce.inventory", "com.ecommerce.common.web.filter", "com.ecommerce.common.web.config", "com.ecommerce.common.web.interceptor"})
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
