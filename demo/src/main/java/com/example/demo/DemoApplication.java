package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 
 */
@SpringBootApplication(scanBasePackages = {
        "com.example.demo",
        "com.example.common"
})
@MapperScan({"com.example.common.mapper"}) ////指定mapper包的位置，告诉springboot我的mapper接口在哪，项目启动时回家再所有的接口文件
@EnableDiscoveryClient
@EnableFeignClients
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
