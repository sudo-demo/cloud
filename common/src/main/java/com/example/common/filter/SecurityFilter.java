package com.example.common.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.domain.LoginUser;
import com.example.common.domain.User;
import com.example.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
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

//    @Autowired
//    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Resource
    JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        log.info("访问的链接是:{}", httpServletRequest.getRequestURI());




//        try {
//            HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(httpServletRequest);
//            if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod) {
//                HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
//                System.out.println("访问的控制器：" +handlerMethod.getBeanType().getSimpleName());
//                System.out.println("访问的控制器：" +handlerMethod.getMethod().getName());
//                // 在这里可以使用handlerMethod进行相关操作
//            }
//        } catch (Exception e) {
//            // 处理异常
//        }

//        log.info("访问的链接是:{}", httpServletRequest.getPathInfo());
//        log.info("访问的链接是:{}", httpServletRequest.getContextPath());
//        log.info("访问的链接是:{}", httpServletRequest.getServletPath());
//        try {
//            final String token = httpServletRequest.getHeader("token");
//            LambdaQueryWrapper<SysUserToken> condition = Wrappers.<SysUserToken>lambdaQuery().eq(SysUserToken::getToken, token);
//            SysUserToken sysUserToken = sysUserTokenService.getOne(condition);
//            if (Objects.nonNull(sysUserToken)) {
//                SysUser sysUser = sysUserService.getById(sysUserToken.getUserId());
//                if (Objects.nonNull(sysUser)) {
//                    SecurityUser securityUser = securityUserService.loadUserByUsername(sysUser.getLoginName());
//                    //将主体放入内存
//                    UsernamePasswordAuthenticationToken authentication =
//                            new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
//                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
//                    //放入内存中去
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                }
//            }
//        } catch (Exception e) {
//            log.error("认证授权时出错:{}", Arrays.toString(e.getStackTrace()));
//        }


//        LoginUser loginUser = new LoginUser();
//        User user = new User();
//        user.setLoginId("1");
//        user.setUserName("姓名");
//        loginUser.setUser(user);

        User user = jwtUtil.getLoginUser(httpServletRequest);
        if(!ObjectUtil.isEmpty(user)){
            LoginUser loginUser = new LoginUser();
            loginUser.setUser(user);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser,null);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }
}
