package com.example.demo.controller;

import com.example.common.domain.SystemUser;
import com.example.common.model.PageResult;
import com.example.demo.service.DemoService;
import com.example.demo.service.impl.Demo1ServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.feign.SystemClient;
import com.example.common.model.PageDTO;

import javax.annotation.Resource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

/**
 * 
 */
@RestController
@RequestMapping("demo")//要访问这个类下所有方法，路径必须有/user
@Api(value = "demo", tags = "demo模块")
public class Demo {
    @Resource
    SystemClient systemClient;

    @Resource
    Map<String, DemoService> demoServiceMap;


    @ApiOperation("index")
    @PostMapping("/index")
    public PageResult<SystemUser> index(@RequestBody PageDTO pageDTO) {
        return systemClient.list(pageDTO);
    }

    @ApiOperation("demo")
    @PostMapping("/demo")
    public void demo(String name) {

        DemoService orDefault = demoServiceMap.getOrDefault(
                name,
                //Proxy.newProxyInstance  动态创建代理类
                (DemoService) Proxy.newProxyInstance(
                        getClass().getClassLoader(),
                        new Class[]{DemoService.class},
//                        (proxy, method, args) -> null
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                // 在这里处理方法调用，本例中不执行任何操作，直接返回 null
                                System.out.println(proxy);
                                return null;
                            }
                        }
                )
        );
        orDefault.demo2();
    }
}
