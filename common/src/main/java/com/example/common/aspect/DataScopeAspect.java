package com.example.common.aspect;

import com.example.common.annotation.DataScope;
import com.example.common.config.Security.PermissionService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 
 */
@Aspect
@Component
public class DataScopeAspect {

    @Resource
    PermissionService permissionService;

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) {
        Class<?> clazz = controllerDataScope.clazz();
        String callMethod = controllerDataScope.callMethod();
        permissionService.getContext().setClazz(clazz);
        permissionService.getContext().setCallMethod(callMethod);
//        try {
//            // 获取并调用方法
//            Method method = clazz.getMethod(callMethod);
//            Object o = clazz.newInstance();
//            method.invoke(o);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
