package com.example.common.Interceptor;

import java.lang.reflect.Method;

public interface ProxyInterceptor {

    /**
     * 方法调用前执行
     *
     * @param method 调用的方法
     * @param args   方法参数
     */
    void before(Method method, Object[] args);

    /**
     * 方法调用后执行
     *
     * @param method 调用的方法
     * @param args   方法参数
     * @param result 方法返回值
     */
    void after(Method method, Object[] args, Object result);

    /**
     * 方法调用异常时执行
     *
     * @param method 调用的方法
     * @param args   方法参数
     * @param e      异常
     */
    void afterException(Method method, Object[] args, Throwable e);

}
