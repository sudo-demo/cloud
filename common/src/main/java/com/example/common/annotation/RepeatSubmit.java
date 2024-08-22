package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 防止重复提交
 */
@Inherited // 允许子类继承
@Target({ElementType.METHOD, ElementType.TYPE}) // 允许在方法和类上使用
@Retention(RetentionPolicy.RUNTIME) // 注解在运行时可用
@Documented //生成文档时包含该注解
public @interface RepeatSubmit {

    /**
     * 加锁过期时间，默认是5秒
     */
    long lockTime() default 5L;
}
