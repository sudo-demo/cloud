//package com.example.common.config.Security;
//
//import com.alibaba.fastjson.JSON;
//import com.example.common.config.Security.CustomAuthorizationManager;
//import com.example.common.filter.SecurityFilter;
//import com.example.common.model.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import javax.annotation.Resource;
//
//
//@Configuration
//public class SpringSecurityConfigurer extends WebSecurityConfigurerAdapter {
//
//    @Bean
//    @Override
//    public AuthenticationManager authenticationManagerBean() throws Exception {
//        return super.authenticationManagerBean();
//    }
//
//    @Bean
//    public PasswordEncoder password() {
//        return new PasswordEncoder() {
//            @Override
//            public String encode(CharSequence rawPassword) {
//                return rawPassword.toString();
//            }
//
//            @Override
//            public boolean matches(CharSequence rawPassword, String encodedPassword) {
//                return rawPassword.toString().equals(encodedPassword);
//            }
//        };
//    }
//
//    @Resource
//    SecurityFilter securityFilter;
//
//
//
//    @Resource
//    CustomAuthorizationManager customAuthorizationManager;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(
//                        (authorize) -> authorize
//                                .antMatchers(new String[]{"/auth/**","/oauth/**","/doc.html","/webjars/**","/swagger-resources/**","/v*/api-docs/**","/Login/login"}).permitAll()
//                                .anyRequest().access(customAuthorizationManager) // 其他请求使用自定义授权管理器
//                ).csrf().disable() // 禁用 CSRF
//                .cors().disable() // 禁用 CORS
//                .exceptionHandling().authenticationEntryPoint(failureHandling())// 处理未授权异常
//                .accessDeniedHandler(accessDeniedHandler())// 处理无权限异常
//                .and().headers().frameOptions().disable()// 禁用框架选项
//                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 设置无状态会话
//                .and().addFilterBefore(securityFilter,UsernamePasswordAuthenticationFilter.class)  // 添加自定义过滤器
//                .httpBasic().disable();// 禁用基本认证
//
//    }
//
//
//    /**
//     * 认证过的用户访问无权限资源时的异常
//     *
//     * @return
//     */
//    public static AccessDeniedHandler accessDeniedHandler() {
//        return (request, response, exception) -> {
////            exception.printStackTrace();
////            response.setStatus(200);
//            response.setContentType("application/json");
//            response.setCharacterEncoding("utf-8");
//            response.getWriter().write(JSON.toJSONString(Result.error(403, "无接口权限")));
//        };
//    }
//
//    /**
//     * 匿名用户访问无权限资源时的异常
//     *
//     * @return
//     */
//    public static AuthenticationEntryPoint failureHandling() {
//        return (request, response, exception) -> {
//            exception.printStackTrace();
////            response.setStatus(200);
//            response.setContentType("application/json");
//            response.setCharacterEncoding("utf-8");
//            response.getWriter().write(JSON.toJSONString(Result.error(401, "未授权")));
//        };
//    }
//
//}
