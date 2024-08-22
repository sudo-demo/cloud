package com.example.common.aspect;

import com.example.common.annotation.RepeatSubmit;
import com.example.common.util.HttpUtils;
import com.example.common.util.RedisUtil;
import com.example.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Aspect
public class RepeatSubmitAspect {

    /**
     * 环绕通知：检查是否存在重复提交请求
     *
     * @param joinPoint    连接点，封装了目标方法信息
     * @param repeatSubmit @RepeatSubmit 注解，包含了锁定时长等信息
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@within(repeatSubmit) || @annotation(repeatSubmit)") // 切入点：方法或类上有 @RepeatSubmit 注解
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        // 获取当前请求的唯一标识
        Object[] args = joinPoint.getArgs();
        // 如果是类级别的注解
        if (repeatSubmit == null) {
            Class<?> targetClass = joinPoint.getTarget().getClass();
            repeatSubmit = targetClass.getAnnotation(RepeatSubmit.class);
        }
        String requestUri = HttpUtils.getRequest().getRequestURI();

        String requestKey = getRequestKey(String.valueOf(SecurityUtils.getUserId()), requestUri, args);
        // 获取当前时间
        long currentTime = System.currentTimeMillis();

        // 检查是否存在重复提交
        Long lockTime = (Long) RedisUtil.get(requestKey);
        if (lockTime != null && currentTime < lockTime) {
            log.warn("重复提交请求: {}", requestKey);
            throw new IllegalStateException("请勿重复提交请求");
        }

        // 设置新的锁定时间
        long lockDuration = TimeUnit.SECONDS.toMillis(repeatSubmit.lockTime());
        RedisUtil.set(requestKey, currentTime + lockDuration);
        try {
            // 执行目标方法
            return joinPoint.proceed();
        } finally {
            // 方法执行完毕后，移除锁定
            RedisUtil.del(requestKey);
        }
    }

    // 获取当前请求的唯一标识，通过参数生成哈希值
    private String getRequestKey(String userId, String requestUri, Object[] args) {
        try {
            // 将用户ID和参数数组转换为字符串并生成哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = userId + requestUri + Arrays.toString(args);
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); // 返回哈希值作为请求唯一标识
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("哈希算法不可用", e);
        }
    }

}
