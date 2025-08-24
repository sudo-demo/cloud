package com.example.common.aspect;

import com.example.common.annotation.RepeatSubmit;
import com.example.common.util.HttpUtils;
import com.example.common.util.RedisUtil;
import com.example.common.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

@Component
@Slf4j
@Aspect
public class RepeatSubmitAspect {

    private static final String REPEAT_SUBMIT_PREFIX = "repeat_submit:";

    @Resource
    private RedisUtil redisUtil;

    // SpEL表达式解析器
    private final ExpressionParser parser = new SpelExpressionParser();
    private final LocalVariableTableParameterNameDiscoverer discoverer =
            new LocalVariableTableParameterNameDiscoverer();

    /**
     * 环绕通知：检查是否存在重复提交请求
     */
    @Around("@within(repeatSubmit) || @annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        // 获取有效的注解配置（方法级别优先）
        RepeatSubmit effectiveAnnotation = getEffectiveAnnotation(joinPoint, repeatSubmit);
        if (effectiveAnnotation == null) {
            return joinPoint.proceed();
        }

        // 生成请求唯一标识和锁值
        String requestKey = REPEAT_SUBMIT_PREFIX + getRequestKey(joinPoint, effectiveAnnotation);
        String lockValue = UUID.randomUUID().toString();

        try {
            // 使用RedisUtil的原子加锁方法
            boolean lockAcquired = redisUtil.tryLock(requestKey, lockValue, effectiveAnnotation.lockTime(), effectiveAnnotation.timeUnit());

            if (!lockAcquired) {
                log.warn("重复提交请求被拒绝: {}", requestKey);
                throw new IllegalStateException(effectiveAnnotation.message());
            }

            log.debug("成功获取重复提交锁: {}", requestKey);

            // 执行目标方法
            return joinPoint.proceed();

        } finally {
            // 使用RedisUtil的原子解锁方法
            boolean unlocked = redisUtil.unlock(requestKey, lockValue);
            if (unlocked) {
                log.debug("成功释放锁: {}", requestKey);
            } else {
                log.debug("锁释放失败或已自动过期: {}", requestKey);
            }
        }
    }

    /**
     * 获取有效的注解配置（方法级别优先于类级别）
     */
    private RepeatSubmit getEffectiveAnnotation(ProceedingJoinPoint joinPoint, RepeatSubmit classAnnotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 首先检查方法上的注解
        RepeatSubmit methodAnnotation = signature.getMethod().getAnnotation(RepeatSubmit.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // 如果没有方法级别注解，使用类级别注解
        return classAnnotation != null ? classAnnotation :
                joinPoint.getTarget().getClass().getAnnotation(RepeatSubmit.class);
    }

    /**
     * 获取请求唯一标识
     */
    private String getRequestKey(ProceedingJoinPoint joinPoint, RepeatSubmit annotation) {
        try {
            // 1. 优先处理自定义key（SpEL表达式）
            String customKey = annotation.key();
            if (customKey != null && !customKey.trim().isEmpty()) {
                String evaluatedKey = evaluateSpEL(customKey, joinPoint);
                // 自定义key已经包含用户信息（如果表达式中有的话）
                return generateSHA256Hash("custom:" + evaluatedKey);
            }

            // 2. 默认key生成逻辑（始终包含用户信息，可选包含参数）
            StringBuilder identifierBuilder = new StringBuilder();

            // 用户标识（始终包含）
            identifierBuilder.append("user:").append(getUserIdentifier()).append(":");

            // 请求URI（始终包含）
            identifierBuilder.append("uri:").append(getRequestUri()).append(":");

            // 方法签名（始终包含）
            identifierBuilder.append("method:").append(joinPoint.getSignature().toShortString()).append(":");

            // 参数内容（根据includeParams配置）
            if (annotation.includeParams()) {
                Object[] args = joinPoint.getArgs();
                String argsHash = generateArgsHash(args);
                identifierBuilder.append("args:").append(argsHash);
            } else {
                identifierBuilder.append("args:excluded");
            }

            // 生成最终哈希
            return generateSHA256Hash(identifierBuilder.toString());

        } catch (Exception e) {
            log.error("生成请求标识失败，使用备用方案", e);
            return "fallback:" + UUID.randomUUID();
        }
    }

    /**
     * 获取用户标识（始终包含）
     */
    private String getUserIdentifier() {
        try {
            return String.valueOf(SecurityUtil.getUserId());
        } catch (Exception e) {
            log.debug("无法获取用户ID，尝试使用IP地址");
            try {
                String ip = HttpUtils.getRequest().getRemoteAddr();
                return "ip:" + ip;
            } catch (Exception ex) {
                return "unknown";
            }
        }
    }

    /**
     * 获取请求URI（始终包含）
     */
    private String getRequestUri() {
        try {
            return HttpUtils.getRequest().getRequestURI();
        } catch (Exception e) {
            return "unknown_uri";
        }
    }

    /**
     * 解析SpEL表达式
     */
    private String evaluateSpEL(String expression, ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = discoverer.getParameterNames(signature.getMethod());
            Object[] args = joinPoint.getArgs();

            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Expression expr = parser.parseExpression(expression);
            Object value = expr.getValue(context);
            return value != null ? value.toString() : "null";
        } catch (Exception e) {
            log.warn("SpEL表达式解析失败: {}", expression, e);
            return "spel_error";
        }
    }

    /**
     * 生成参数哈希
     */
    private String generateArgsHash(Object[] args) {
        if (args == null || args.length == 0) {
            return "no_args";
        }

        try {
            String argsString = Arrays.deepToString(args);
            if (argsString.length() > 1000) {
                String truncated = argsString.substring(0, 500) + "..." +
                        argsString.substring(argsString.length() - 500);
                return generateSHA256Hash(truncated);
            }
            return generateSHA256Hash(argsString);
        } catch (Exception e) {
            log.warn("参数序列化失败，使用参数个数作为标识", e);
            return "args_count:" + args.length;
        }
    }

    /**
     * 生成SHA256哈希
     */
    private String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}