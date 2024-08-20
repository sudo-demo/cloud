package com.example.common.aspect;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.common.config.Security.PermissionService;
import com.example.common.domain.SystemLog;
import com.example.common.service.SystemLogService;
import com.example.common.util.HttpUtils;
import com.example.common.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;


/**
 * 
 */
@Aspect
@Component
public class LogAspect {

    @Resource
    SystemLogService systemLogService;

    @Resource
    PermissionService permissionService;

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
        String jsonStr = JSONUtil.toJsonStr(result);
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);

        SystemLog systemLog = new SystemLog();
        systemLog.setUserId(SecurityUtils.getUserId())
                .setUsername(SecurityUtils.getUserName())
                .setRoleId(SecurityUtils.getRoleId())
                .setApiName(permissionService.getContext().getApiName())
                .setApiUrl(Objects.requireNonNull(HttpUtils.getRequest()).getRequestURI())
                .setRequest(HttpUtils.getRequestParam())
                .setResponse(JSONUtil.toJsonStr(result))
                .setCode(jsonObject.getStr("code"))
                .setResponseMessage(jsonObject.getStr("message"))
                .setIp(HttpUtils.getIpAddress());
//                .setCreatedAt(Date.from(Instant.now()));

        systemLogService.saveLog(systemLog);

//        System.out.println("请求："+HttpUtils.getRequestParam());
//        System.out.println("请求："+HttpUtils.getRequest().getMethod());
//
//        System.out.println("方法："+joinPoint.getSignature().getName());
//        System.out.println("返回："+JSONUtil.toJsonStr(result));
    }

}
