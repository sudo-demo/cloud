package com.example.common.util.proxy;

import java.lang.reflect.*;

public class ProxyUtils {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class must not be null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Class must be an interface");
        }

        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    T implInstance = loadImplInstance(interfaceClass);
//                    return method.invoke(implInstance, args);
                    return "测试";
                });
    }

    private static <T> T loadImplInstance(Class<T> interfaceClass) {
        String implClassName = "com.example.demo.service.impl.Demo1ServiceImpl";
        try {
            Class<?> implClass = Class.forName(implClassName);
            if (!interfaceClass.isAssignableFrom(implClass)) {
                throw new IllegalStateException(implClassName + " does not implement " + interfaceClass.getName());
            }
            @SuppressWarnings("unchecked")
            T instance = (T) implClass.getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load implementation " + implClassName, e);
        }
    }
}
