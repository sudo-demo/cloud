package com.example.common.annotation;

import com.example.common.config.Mybatis.DataScopeInterceptor;

import java.lang.annotation.*;

/**
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 主表的别名
     */
    String masterAlias() default "";

    /**
     * mapperId
     */
    String mappedStatementId() default "";

    /**
     * 回调类
     */
    Class<?> clazz() default DataScopeInterceptor.class;

    /**
     * 回调方法名称
     *  用于自己拼接sql
     */
    String callMethod() default "handleDataScope";
}
