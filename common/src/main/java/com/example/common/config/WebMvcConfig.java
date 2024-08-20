package com.example.common.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


@EnableWebMvc
@Configuration
public class WebMvcConfig  implements WebMvcConfigurer {

    @Resource
    FileUploadConfig fileUploadConfig;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** swagger配置 */
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

        // 上传文件资源
        registry.addResourceHandler(fileUploadConfig.getStaticAccessPath()+"**").addResourceLocations("file:"+fileUploadConfig.getUploadDir());
    }


}
