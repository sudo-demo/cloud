package com.example.common.aspect;

import cn.hutool.json.JSONUtil;
import com.example.common.util.HttpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;


/**
 * 
 */
@Aspect
@Component
public class LogAspect {

    // 定义一个切点，这里使用表达式匹配所有 com.example.service 包下的所有方法调用
    @Pointcut("@annotation(com.example.common.annotation.Log)")
    public void serviceMethods() {}

    // 在 serviceMethods 切点的方法执行前执行
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Before method: " + joinPoint.getSignature());
    }

    // 在 serviceMethods 切点的方法执行后执行
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        System.out.println("请求："+HttpUtils.getRequestParam());
        System.out.println("请求："+HttpUtils.getRequest().getMethod());

        System.out.println("方法："+joinPoint.getSignature().getName());
        System.out.println("返回："+JSONUtil.toJsonStr(result));
    }

}
