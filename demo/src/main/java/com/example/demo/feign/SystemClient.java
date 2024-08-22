package com.example.demo.feign;

import com.example.common.domain.SystemUser;
import com.example.common.model.PageDTO;
import com.example.common.model.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @FeignClient(
 *     name = "service-name",          // 服务名称，必填，表示要调用的服务的名称
 *     url = "http://localhost:8080", // 可选，服务的 URL，通常不需要，使用服务发现时可省略
 *     path = "/api",                  // 可选，服务的基础路径，所有请求都会加上这个路径
 *     fallback = FallbackClass.class, // 可选，指定降级处理类
 *     fallbackFactory = FallbackFactory.class, // 可选，指定降级工厂类
 *     configuration = FeignConfig.class, // 可选，指定 Feign 的配置类
 *     primary = true,                 // 可选，是否为主 Bean，默认为 true
 *     qualifier = "myFeignClient"     // 可选，指定 Bean 的名称
 * )
 *
 */
@FeignClient(name = "system",url = "${feign.system.url}",path = "${feign.system.path}")
public interface SystemClient {

    @PostMapping("user/page")
    PageResult<SystemUser> list(@RequestBody PageDTO pageDTO);
}
