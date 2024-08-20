package com.example.common.config.Security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Supplier;

@Slf4j
@Component
public class CustomAuthorizationManager implements AuthorizationManager <RequestAuthorizationContext>{


    @Resource
    PermissionService permissionService;


    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {

        if(authentication.get().getPrincipal().equals("anonymousUser")){
            return new AuthorizationDecision(false);
        }
        try{
            if(!permissionService.verifyAuth(object.getRequest())){
                return new AuthorizationDecision(false);
            }
        }catch (Exception exception){
            log.error("权限验证失败",exception);
            exception.printStackTrace();
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(true);

    }

}
