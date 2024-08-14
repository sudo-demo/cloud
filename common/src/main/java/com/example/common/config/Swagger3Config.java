package com.example.common.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Contact;
import io.swagger.models.HttpMethod;
import io.swagger.models.Response;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableOpenApi //注解启动用Swagger的使用，同时在配置类中对Swagger的通用参数进行配置
public class Swagger3Config implements EnvironmentAware {

    private String applicationName;

    private String applicationDescription;

    @Override
    public void setEnvironment(Environment environment) {
//        this.applicationDescription = environment.getProperty("spring.application.description");
        this.applicationDescription = "描述";
        this.applicationName = environment.getProperty("spring.application.name");
    }

    @Bean
    public Docket createRestApi(){
        //返回文档概要信息
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build();

    }

    /*
    生成接口信息，包括标题，联系人等
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(applicationName+"接口文档")
                .description(applicationDescription)
                .version("1.0")
                .build();
    }




}
