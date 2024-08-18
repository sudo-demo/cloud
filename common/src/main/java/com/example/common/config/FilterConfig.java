//package com.example.common.config;
//
//import com.example.common.filter.RepeatableFilter;
//import com.example.common.filter.SecurityFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FilterConfig {
//
//    @Bean
//    public FilterRegistrationBean<RepeatableFilter> repeatableFilter() {
//        FilterRegistrationBean<RepeatableFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new RepeatableFilter());
//        registrationBean.setOrder(1); // 设置顺序为 1
//        return registrationBean;
//    }
//
//    @Bean
//    public FilterRegistrationBean<SecurityFilter> securityFilter() {
//        FilterRegistrationBean<SecurityFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new SecurityFilter());
//        registrationBean.setOrder(2); // 设置顺序为 2
//        return registrationBean;
//    }
//}
