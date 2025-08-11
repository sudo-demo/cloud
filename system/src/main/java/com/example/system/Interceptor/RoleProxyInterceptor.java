package com.example.system.Interceptor;

import com.example.common.Interceptor.ProxyInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;

public class RoleProxyInterceptor implements ProxyInterceptor {
    @Override
    public void before(Method method, Object[] args) {
        System.out.println("直接前拦截："+Arrays.toString(args));
    }

    @Override
    public void after(Method method, Object[] args, Object result) {
        System.out.println("直接后拦截："+Arrays.toString(args));
        System.out.println("直接后拦截："+result);
    }

    @Override
    public void afterException(Method method, Object[] args, Throwable e) {
        if(e instanceof com.example.common.exception.validateException){
            System.out.println("参数错误");
        }
        System.out.println("直接异常拦截："+Arrays.toString(args));
        System.out.println("直接异常拦截："+e.getCause()+e.getMessage());
    }
}
