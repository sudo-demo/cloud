package com.example.common.annotation;

import java.lang.annotation.*;

/**
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    String apiName() default "";
}
