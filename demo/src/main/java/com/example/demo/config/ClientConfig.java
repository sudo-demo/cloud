package com.example.demo.config;

import com.client.aop.LogAspectImpl;
import com.client.domain.ClientTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public ClientTemplate setConfig() {
        LogAspectImpl logAspect = new LogAspectImpl();
        ClientTemplate clientTemplate = new ClientTemplate();
        clientTemplate.setLogAspect(logAspect);
        clientTemplate.setMethod("get");
        clientTemplate.setUrl("http://localhost:8080/");
        clientTemplate.setArgs(new Object[]{1, 2, 3});
        return clientTemplate;
    }
}
