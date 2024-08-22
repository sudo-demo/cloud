package com.example.common.config.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * 自定义授权管理器，用于处理请求的权限验证
 */
@Slf4j
@Component
public class CustomAuthorizationManager implements AuthorizationManager <RequestAuthorizationContext>{


    @Resource
    PermissionService permissionService;// 注入权限服务，用于验证权限


    /**
     * 验证方法，通常用于自定义逻辑
     *
     * @param authentication 认证信息的 Supplier
     * @param object         请求授权上下文
     */
    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        // 调用父类的 verify 方法，通常不需要自定义实现
        AuthorizationManager.super.verify(authentication, object);
    }


    /**
     * 检查请求的授权情况
     *
     * @param authentication 认证信息的 Supplier
     * @param object         请求授权上下文
     * @return AuthorizationDecision 授权决策，表示是否允许访问
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        // 检查用户是否为匿名用户
        if(authentication.get().getPrincipal().equals("anonymousUser")){
            return new AuthorizationDecision(false);// 不允许访问
        }
        try{
            // 调用权限服务验证请求的权限
            if(!permissionService.verifyAuth(object.getRequest())){
//                return new AuthorizationDecision(false);// 权限验证失败，不允许访问
            }
        }catch (Exception exception){
            log.error("权限验证失败",exception); // 记录错误日志
            exception.printStackTrace();// 打印异常堆栈
            return new AuthorizationDecision(false);// 发生异常，不允许访问
        }
        return new AuthorizationDecision(true);// 允许访问

    }

}
