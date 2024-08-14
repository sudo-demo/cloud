package com.example.common.config.Security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Supplier;

@Component
public class CustomAuthorizationManager implements AuthorizationManager <RequestAuthorizationContext>{


    @Resource
    Permission2Service permission2Service;


    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {



//        try {
//            HandlerExecutionChain executionChain = requestMappingHandlerMapping.getHandler(object.getRequest());
//            if (executionChain != null && executionChain.getHandler() instanceof HandlerMethod) {
//                HandlerMethod handlerMethod = (HandlerMethod) executionChain.getHandler();
//
//                RequestMapping methodAnnotation = handlerMethod.getMethodAnnotation(RequestMapping.class);
//                System.out.println("方法  111"+methodAnnotation);
//
//                for (String s : methodAnnotation.path()) {
//                    System.out.println("方法： "+ StrUtil.trim(s,0,e->e=='/'));
//                }
//
//
//                RequestMapping declaredAnnotation = handlerMethod.getBeanType().getDeclaredAnnotation(RequestMapping.class);
//                System.out.println(declaredAnnotation);
//                for (String s : declaredAnnotation.value()) {
//                    System.out.println("控制器： "+StrUtil.trim(s,0,e->e == '/'));
//                }
//
//
////                System.out.println("访问的控制器：" + handlerMethod.getBeanType().getSimpleName());
////                System.out.println("访问的方法：" + handlerMethod.getMethod().getName());
//                // 在这里可以使用handlerMethod进行相关操作
//            }
//        } catch (Exception e) {
//            // 处理异常
//        }


//        if(authentication.get().getPrincipal().equals("anonymousUser")){
//            return new AuthorizationDecision(false);
//        }
//        try{
//            LoginUser principal = (LoginUser) authentication.get().getPrincipal();
//            permissionService.init(object.getRequest());
//            if(!permissionService.verifyAuth()){
////                return new AuthorizationDecision(false);
//            }
//            System.out.println("用户"+principal.getUser());
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return new AuthorizationDecision(false);
//        }
        return new AuthorizationDecision(true);

    }

}
