package com.example.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Act {

    /**
     *  新添加的参数，用于指定要调用的方法名
     */
    String methodToCall() default "";

//    /**
//     * 取其他模块的按钮权限
//     * 示例： {"t6-base/BaseDictUser","t6-manage/BaseCompany"}
//     */
//    String[] appController() default {};


}
