package com.example.common.aspect;

import com.example.common.annotation.Act;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 
 */
@Aspect
@Component
public class ActAspect {

    //@Around 是一种用于定义环绕通知（around advice）的注解。环绕通知可以在方法的调用前后以及抛出异常后执行代码
    @Around("@annotation(com.example.common.annotation.Act)")
    public Object afterAnnotatedMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法执行前的操作
        Object result = joinPoint.proceed();
        // 方法执行后的操作
        // 获取被注解的方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 检查方法上是否存在 @Act 注解
        if (method.isAnnotationPresent(Act.class)) {
            // 获取注解实例
            Act actAnnotation = method.getAnnotation(Act.class);
            // 获取注解的参数值
            String methodToCall = actAnnotation.methodToCall();
            System.out.printf("methodToCall:"+methodToCall);

        }
        return null;
    }
}
