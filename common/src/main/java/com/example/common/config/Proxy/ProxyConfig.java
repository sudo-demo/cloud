package com.example.common.config.Proxy;

import com.example.common.Interceptor.ProxyInterceptor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 代理配置类
 * 管理路由规则和全局拦截器
 */
@Getter
@Configuration
public class ProxyConfig {

    // Getter方法
    /** 路由规则映射：接口全限定名 -> 路由函数 */
    private final Map<String, Function<Object[], String>> providerMap = new ConcurrentHashMap<>();

    /** 全局拦截器（对所有代理生效）
     * -- SETTER --
     *  设置全局拦截器
     */
    @Setter
    private ProxyInterceptor globalInterceptor;

    /**
     * 初始化路由规则
     * 在项目启动时执行，手动配置接口与实现类的映射关系
     */
    @PostConstruct
    public void initRouters() {
        // 示例1：基础路由配置
        addRouter(
                "com.example.system.service.SystemRoleService",
                args -> "com.example.system.service.impl.SystemRoleServiceImpl"
        );

        // 示例2：动态路由配置（根据参数选择实现类）
        addRouter(
                "com.example.service.OrderService",
                args -> {
                    if (args != null && args.length > 0 && "VIP".equals(args[0])) {
                        return "com.example.service.impl.VipOrderServiceImpl";
                    } else {
                        return "com.example.service.impl.NormalOrderServiceImpl";
                    }
                }
        );

        // 可添加更多路由规则...
    }

    /**
     * 添加路由规则
     */
    public void addRouter(String interfaceName, Function<Object[], String> router) {
        if (interfaceName == null || router == null) {
            throw new IllegalArgumentException("接口名和路由函数不能为空");
        }
        providerMap.put(interfaceName, router);
    }

}
