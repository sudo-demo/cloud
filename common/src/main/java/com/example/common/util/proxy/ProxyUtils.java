package com.example.common.util.proxy;

import com.example.common.Interceptor.ProxyInterceptor;
import com.example.common.util.RedisUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 动态代理工具类，通过JDK动态代理实现接口方法调用转发，
 * 代理目标实现类由Redis缓存中的类名动态决定，
 * 实现了接口与具体实现类解耦，支持运行时动态切换实现。
 */
@Slf4j
@Component
public class ProxyUtils implements ApplicationContextAware {

    /**
     * Spring上下文，用于获取具体实现类的Bean实例
     */
    private static ApplicationContext applicationContext;


    /**
     * 代理拦截器，用于在方法调用前后执行自定义逻辑
     * -- SETTER --
     *  设置代理拦截器
     *
     */
    @Getter
    @Setter
    private static ProxyInterceptor proxyInterceptor;

    /**
     * 接口名 -> 通过参数获取实现类全限定名的函数映射
     * <p>
     * 这里示例用Redis缓存实现类名，实际可根据业务扩展
     */
    private static final Map<String, Function<Object[], String>> PROVIDER = new HashMap<String, Function<Object[], String>>() {{
        put("com.example.system.service.SystemRoleService", (args) -> {
//                (String) RedisUtil.get("com.example.system.service.SystemRoleService")
            //判断加载具体的实现类
            return "com.example.system.service.impl.SystemRoleServiceImpl";
        });
        put("com.example.system.service.SystemUserService", (args) -> {
//                (String) RedisUtil.get("com.example.system.service.SystemRoleService")
            //判断加载具体的实现类
            return "com.example.system.service.impl.SystemUserServiceImpl";
        });
    }};

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        ProxyUtils.applicationContext = applicationContext;
    }

    /**
     * 创建指定接口的动态代理实例，代理方法调用会转发给
     * Redis缓存指定的实现类对应的Spring Bean。
     *
     * @param interfaceClass 目标接口Class，必须是接口
     * @param <T>            接口类型
     * @return 代理对象，方法调用会动态路由到实现类Bean
     * @throws IllegalArgumentException 如果接口Class为null或不是接口
     * @throws RuntimeException         加载实现类或调用失败时抛出
     */
    @SuppressWarnings("unchecked")
    public static  <T> T createProxy(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("接口类不能为 null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("参数必须是接口类型");
        }

        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    Object result = null;

                    // 根据接口名和调用参数，从provider中获取实现类的全限定名
                    Function<Object[], String> implNameProvider = PROVIDER.get(interfaceClass.getName());
                    if (implNameProvider == null) {
                        // 未配置实现类，返回null或可抛异常
                        return null;
                    }
                    String implClassName = implNameProvider.apply(args);
                    if (implClassName == null || implClassName.isEmpty()) {
                        // 实现类名为空，直接返回null
                        return null;
                    }

                    T instance;
                    try {
                        // 反射加载实现类Class
                        Class<?> implClass = Class.forName(implClassName);
                        // 从Spring上下文获取实现类Bean实例
                        instance = (T) applicationContext.getBean(implClass);
                    } catch (ClassNotFoundException e) {
                        log.error("实现类未找到: {}", implClassName);
                        return null;
//                        throw new RuntimeException("实现类未找到: " + implClassName, e);
                    } catch (BeansException e) {
                        log.error("Spring容器中未找到实现类Bean: {}", implClassName);
                        return null;
//                        throw new RuntimeException("Spring容器中未找到实现类Bean: " + implClassName, e);
                    }

                    try {
                        // 调用前拦截
                        if (proxyInterceptor != null) {
                            proxyInterceptor.before(method, args);
                        }
                        // 反射调用实现类的方法
                        result = method.invoke(instance, args);
                        // 调用后拦截
                        if (proxyInterceptor != null) {
                            proxyInterceptor.after(method, args, result);
                        }
                        
                    } catch (InvocationTargetException ite) {
                        // 直接抛出目标方法的业务异常
                        Throwable cause = ite.getCause();
                        if (proxyInterceptor != null) {
                            // 传入异常给after，方便处理异常情况
                            proxyInterceptor.afterException(method, args, cause);
                        }
                        throw cause != null ? cause : ite;
                    }finally {
                        // 调用结束后清空全局拦截器，避免影响后续调用
                        ProxyUtils.setProxyInterceptor(null);
                    }

                    return result;
                });
    }
}
