package com.example.system.controller;

import com.example.common.model.Result;
import com.example.system.domain.dto.LoginBody;
import com.example.system.service.SystemUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author huanghongjia
 */
@RestController
@RequestMapping("Login")//要访问这个类下所有方法，路径必须有/user
@Api(value = "登录模块",tags="登录模块")
public class LoginController {


    @Resource
    private SystemUserService systemUserService;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginBody loginBody){
        String token = systemUserService.login(loginBody);

        return Result.success(token);
    }

}
