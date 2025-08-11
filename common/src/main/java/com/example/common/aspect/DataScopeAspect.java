package com.example.common.aspect;

import com.example.common.annotation.DataScope;
import com.example.common.config.Security.PermissionService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
        //没有设置mappedStatementId，则使用当前方法名和类名为key
        String mappedStatementId = controllerDataScope.mappedStatementId();
        if (mappedStatementId == null || mappedStatementId.trim().isEmpty()) {
            String methodName = point.getSignature().getName();
            String className = point.getSignature().getDeclaringTypeName();
            mappedStatementId = className + "." + methodName;
        }

        permissionService.getContext().setClazz(clazz);
        permissionService.getContext().setCallMethod(callMethod);
        permissionService.getContext().setMappedStatementId(mappedStatementId);
        permissionService.getContext().setMasterAlias(controllerDataScope.masterAlias());

    }
}
