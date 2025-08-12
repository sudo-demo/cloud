package com.example.common.util.proxy;

import com.example.common.Interceptor.ProxyInterceptor;
import com.example.common.config.Proxy.ProxyConfig;
import com.example.common.exception.ProxyException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 代理工具类
 * 用于创建接口的动态代理对象
 */
@Slf4j
@Component
public class ProxyUtils implements ApplicationContextAware {

    private static volatile ApplicationContext applicationContext;

    /**
     * 注入配置类（包含路由规则和全局拦截器）
     */
    @Resource
    private ProxyConfig proxyConfig;

    /**
     * 全局缓存开关（默认不启用）
     * -- SETTER --
     * 设置全局缓存开关
     */
    @Setter
    private boolean enableCache = false;

    /**
     * 实例缓存容器
     */
    private final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();


    /**
     * 简化版：创建代理对象
     * 使用全局拦截器和全局缓存开关
     */
    public <T> T createProxy(Class<T> interfaceClass) {
        return createProxy(interfaceClass, null, enableCache);
    }

    public <T> T createProxy(Class<T> interfaceClass, ProxyInterceptor localInterceptor) {
        return createProxy(interfaceClass, localInterceptor, enableCache);
    }

    /**
     * 扩展版：创建代理对象
     * 支持指定局部拦截器和单次缓存开关
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> interfaceClass, ProxyInterceptor localInterceptor, boolean useCache) {
        // 参数校验
        Assert.notNull(interfaceClass, "接口Class不能为null");
        Assert.isTrue(interfaceClass.isInterface(), "参数必须是接口类型：" + interfaceClass.getName());

        // 创建JDK动态代理
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ProxyInvocationHandler(interfaceClass, localInterceptor, useCache)
        );
    }


    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        if (ProxyUtils.applicationContext == null) {
            synchronized (ProxyUtils.class) {
                if (ProxyUtils.applicationContext == null) {
                    ProxyUtils.applicationContext = applicationContext;
                }
            }
        }
    }


    /**
     * 代理调用处理器
     * 处理代理对象的方法调用逻辑
     */
    private class ProxyInvocationHandler implements java.lang.reflect.InvocationHandler {
        private final Class<?> interfaceClass;
        private final ProxyInterceptor localInterceptor; // 局部拦截器
        private final boolean useCache; // 本次调用是否启用缓存
        private final ProxyInterceptor globalInterceptor = proxyConfig.getGlobalInterceptor(); // 全局拦截器

        public ProxyInvocationHandler(Class<?> interfaceClass, ProxyInterceptor localInterceptor, boolean useCache) {
            this.interfaceClass = interfaceClass;
            this.localInterceptor = localInterceptor;
            this.useCache = useCache;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String interfaceName = interfaceClass.getName();
            Map<String, Function<Object[], String>> providerMap = proxyConfig.getProviderMap();

            // 1. 获取路由规则（不存在时不抛异常）
            Function<Object[], String> router = providerMap.get(interfaceName);
            if (router == null) {
                log.warn("接口[{}]未配置路由规则，返回null", interfaceName);
                return null;
            }

            // 2. 计算实现类名（无效时返回null）
            String implClassName = router.apply(args);
            if (implClassName == null || implClassName.isEmpty()) {
                throw new ProxyException("接口[{" + interfaceName + "}]路由规则返回空实现类名，返回null");
            }

            // 3. 处理方法调用（异常正常抛出）
            try {
                // 获取实现类实例
                Object targetInstance = getTargetInstance(implClassName);
                if (targetInstance == null) {
                    throw new ProxyException("实现类[" + implClassName + "]实例获取失败");
                }

                // 执行前置拦截器（先全局后局部）
                if (globalInterceptor != null) {
                    globalInterceptor.before(method, args);
                }
                if (localInterceptor != null) {
                    localInterceptor.before(method, args);
                }

                // 调用目标方法
                Object result = method.invoke(targetInstance, args);

                // 执行后置拦截器（先全局后局部）
                if (globalInterceptor != null) {
                    globalInterceptor.after(method, args, result);
                }
                if (localInterceptor != null) {
                    localInterceptor.after(method, args, result);
                }

                return result;

            } catch (InvocationTargetException e) {
                // 目标方法本身的异常：原样抛出
                Throwable cause = e.getCause();
                // 执行异常拦截器
                if (globalInterceptor != null) {
                    globalInterceptor.afterException(method, args, cause);
                }
                if (localInterceptor != null) {
                    localInterceptor.afterException(method, args, cause);
                }
                throw cause != null ? cause : e;

            } catch (Exception e) {
                // 代理过程中的异常：包装后抛出
                if (globalInterceptor != null) {
                    globalInterceptor.afterException(method, args, e);
                }
                if (localInterceptor != null) {
                    localInterceptor.afterException(method, args, e);
                }
                throw new ProxyException("代理调用接口[" + interfaceName + "]失败：" + e.getMessage(), e);
            }
        }

        /**
         * 获取实现类实例（支持缓存）
         */
        private Object getTargetInstance(String implClassName) {
            try {
                Class<?> implClass = Class.forName(implClassName);

                // 缓存逻辑（仅当启用缓存时）
                if (useCache) {
                    Object cachedInstance = instanceCache.get(implClass);
                    if (cachedInstance != null) {
                        log.debug("从缓存获取实现类[{}]实例", implClassName);
                        return cachedInstance;
                    }
                }

                // 从Spring容器获取实例
                Object instance = applicationContext.getBean(implClass);
                log.debug("从Spring容器获取实现类[{}]实例", implClassName);

                // 存入缓存（仅当启用缓存时）
                if (useCache) {
                    instanceCache.put(implClass, instance);
                }

                return instance;

            } catch (Exception e) {
                log.error("获取实现类[{}]实例失败", implClassName, e);
                return null;
            }
        }
    }
}
