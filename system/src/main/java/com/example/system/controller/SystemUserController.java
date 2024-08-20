package com.example.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.annotation.Log;
import com.example.system.domain.SystemUser;
import com.example.system.domain.dto.SystemUserDto;
import com.example.system.service.SystemUserService;
import com.example.common.constants.Add;
import com.example.common.constants.Update;
import com.example.common.model.PageDTO;
import com.example.common.model.PageResult;
import com.example.common.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController//@RestController默认情况下，@RestController注解会将返回的赌侠ing数据转换为JSON格式，这样方便前端用`。
@RequestMapping("user")//要访问这个类下所有方法，路径必须有/user
@Api(value="用户管理",tags = "用户管理")
public class SystemUserController {

    @Autowired
    private SystemUserService systemUserService;

    @ApiOperation("新增用户")
    @PostMapping("add")
    public Result add(@Validated(Add.class) @RequestBody SystemUserDto systemUserDto){
        System.out.println(systemUserDto);
        systemUserService.created(systemUserDto);
        return Result.success();

    }

    @Log
    @ApiOperation("修改用户")
    @PostMapping("update")
    public Result updated(@Validated(Update.class) @RequestBody SystemUserDto systemUserDto){
        systemUserService.updated(systemUserDto);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @GetMapping("remove")
    public Result<Void> remove(@Valid  Integer userId){
        systemUserService.remove(userId);
        return Result.success();
    }


//    @Act(methodToCall = "execution(com.example.system.service.impl.getAct)")
    @ApiOperation("用户列表")
    @PostMapping("page")
    public PageResult<SystemUser> list(@RequestBody PageDTO pageDTO) throws Exception {
        IPage<SystemUser> userPage = systemUserService.getUserPage(pageDTO);
        return PageResult.of(userPage);

    }

    @ApiOperation("用户详情")
    @GetMapping("info")
    public Result<SystemUser> info(@Valid  Integer userId){
        SystemUser info = systemUserService.getInfo(userId);
        return Result.success(info);
    }

}
