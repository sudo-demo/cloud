package com.example.system.controller;

import com.example.common.model.Result;
import com.example.common.util.proxy.ProxyUtils;
import com.example.system.Interceptor.RoleProxyInterceptor;
import com.example.system.domain.dto.SystemRoleDto;
import com.example.system.domain.dto.SystemUserDto;
import com.example.system.service.SystemRoleService;
import com.example.system.service.SystemUserService;
import com.example.system.service.impl.SystemRoleServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("Demo")//要访问这个类下所有方法，路径必须有/user
@Api(value = "测试",tags="测试")
public class DemoController {

//    @Resource
//    ProxyUtils proxyUtils;

    @Resource
    SystemRoleService systemRoleService;

    @ApiOperation("测试1")
    @PostMapping("demo1")
    public Result<Void> demo1() {
        SystemRoleDto systemRoleDto = new SystemRoleDto();
        systemRoleDto.setRoleId(21L);
        systemRoleDto.setRoleName("测试");
        systemRoleDto.setStatus(1);
        systemRoleDto.setSort(1);
        systemRoleDto.setRemark("测试");

        ProxyUtils.setProxyInterceptor(new RoleProxyInterceptor());
        ProxyUtils.createProxy(SystemRoleService.class).created(systemRoleDto);

        SystemUserDto systemUserDto = new SystemUserDto();
        ProxyUtils.createProxy(SystemUserService.class).created(systemUserDto);

//        proxyUtils.setImplClassName(SystemRoleServiceImpl.class)
//                .createProxy(SystemRoleService.class)
//                .created(systemRoleDto);

//        proxyUtils.createProxy(SystemRoleService.class).created(systemRoleDto);

//        systemRoleService.created(systemRoleDto);
        return Result.success();
    }
}
