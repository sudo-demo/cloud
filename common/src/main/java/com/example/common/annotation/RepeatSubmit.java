package com.example.common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交注解
 * 可应用于方法或类，类级别注解会被方法继承
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 加锁过期时间（默认2秒）
     */
    long lockTime() default 2L;

    /**
     * 时间单位（默认秒）
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 自定义锁key，支持SpEL表达式
     * 例如: #user.id, #order.orderNo
     * 如果设置，将优先使用自定义key
     */
    String key() default "";

    /**
     * 重复提交时的错误消息
     */
    String message() default "请勿重复提交请求";

    /**
     * 是否包含请求参数生成锁key（默认false）
     * 仅在未设置自定义key时生效
     */
    boolean includeParams() default false;
}