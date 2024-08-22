package com.example.common.config;

import com.alibaba.fastjson.JSON;
import com.example.common.config.Security.CustomAuthorizationManager;
import com.example.common.constants.CommonConstants;
import com.example.common.filter.RepeatableFilter;
import com.example.common.filter.SecurityFilter;
import com.example.common.model.Result;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

/**
 * SecurityConfig 类用于配置 Spring Security 的安全设置。
 */
@EnableWebSecurity // 启用 Web 安全
public class SecurityConfig {

    @Resource
    private SecurityFilter securityFilter;//身份验证过滤器

    @Resource
    private CustomAuthorizationManager customAuthorizationManager;//权限验证过滤器

    /**
     * 配置 SecurityFilterChain，定义 HTTP 请求的安全策略。
     *
     * @param http HttpSecurity 对象
     * @return SecurityFilterChain 对象
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(CommonConstants.WHITE_LIST).permitAll() // 白名单，允许匿名访问
                        .anyRequest().access(customAuthorizationManager) // 自定义授权管理器
                )
                .csrf().disable() // 禁用 CSRF
                .cors().disable() // 禁用 CORS
                .exceptionHandling()
                .authenticationEntryPoint(failureHandling()) // 处理未授权异常
                .accessDeniedHandler(accessDeniedHandler()) // 处理无权限异常
                .and()
                .headers().frameOptions().disable() // 禁用框架选项
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 设置无状态会话
                .and()
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) // 添加自定义过滤器
                .httpBasic().disable(); // 禁用基本认证

        return http.build(); // 返回构建的 SecurityFilterChain
    }

    /**
     * 定义密码编码器，用于处理密码的加密和匹配。
     *
     * @return PasswordEncoder 对象
     */
    @Bean
    public PasswordEncoder password() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();// 直接返回原始密码（不加密）
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);// 比较原始密码和编码后的密码
            }
        };
    }

    /**
     * 配置 AuthenticationManager，用于身份验证管理。
     *
     * @param http HttpSecurity 对象
     * @return AuthenticationManager 对象
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        // 在这里配置 userDetailsService 和 passwordEncoder
        return authenticationManagerBuilder.build();
    }

    /**
     * 处理无权限访问的异常，返回 403 错误。
     *
     * @return AccessDeniedHandler 对象
     */
    public static AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(JSON.toJSONString(Result.error(403, "无接口权限"))); // 返回403错误
        };
    }

    /**
     * 处理未授权访问的异常，返回 401 错误。
     *
     * @return AuthenticationEntryPoint 对象
     */
    public static AuthenticationEntryPoint failureHandling() {
        return (request, response, exception) -> {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(JSON.toJSONString(Result.error(401, "未授权"))); // 返回401错误
        };
    }
}
