package com.example.common.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.context.LoginUserContextHolder;
import com.example.common.domain.LoginUser;
import com.example.common.domain.User;
import com.example.common.util.JwtUtil;
import com.example.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Resource
    RedisUtil redisUtil;
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull FilterChain filterChain) throws ServletException, IOException {

        Object userObject = redisUtil.get(JwtUtil.getTokenKey(httpServletRequest)); // 从Redis获取用户信息
        if(ObjectUtil.isNotEmpty(userObject)){
            User user = BeanUtil.copyProperties(userObject, User.class);
            LoginUser loginUser = new LoginUser();
            loginUser.setUser(user);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser,null);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            LoginUserContextHolder.setLoginUser(loginUser);//设置 LoginUserContextHolder
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }
}
