package com.example.common.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.example.common.config.Mybatis.DataScopeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * MybatisPlusConfig 配置类
 * 用于配置 MyBatis-Plus 的拦截器和自定义配置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MybatisPlusInterceptor 实例
     * @return MybatisPlusInterceptor 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());//分页拦截器

        // 添加自定义的数据权限处理器
        return interceptor;
    }

    @Resource
    DataScopeInterceptor dataScopeInterceptor;// 注入自定义的数据权限处理器

    /**
     * 自定义 MyBatis 配置
     * @return ConfigurationCustomizer 自定义配置
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            // 创建并添加拦截器实例到配置中
            configuration.addInterceptor(dataScopeInterceptor);
        };
    }

}
