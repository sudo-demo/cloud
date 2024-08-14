package com.example.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.system.domain.SystemRole;
import com.example.system.domain.dto.SystemRoleDto;
import com.example.system.service.SystemRoleService;
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
@RequestMapping("role")//要访问这个类下所有方法，路径必须有/user
@Api(value="角色管理",tags = "角色管理")
public class SystemRoleController {


    @Autowired
    private SystemRoleService service;

    @ApiOperation("新增角色")
    @PostMapping("add")
    public Result add(@Validated(Add.class) @RequestBody SystemRoleDto dto){
        service.created(dto);
        return Result.success();

    }

    @ApiOperation("修改角色")
    @PostMapping("update")
    public Result update(@Validated(Update.class) @RequestBody SystemRoleDto dto){
        service.updated(dto);
        return Result.success();
    }

    @ApiOperation("删除角色")
    @GetMapping("remove")
    public Result<Void> remove(@Valid  Integer roleId){
        service.remove(roleId);
        return Result.success();
    }


    @ApiOperation("角色列表")
    @PostMapping("page")
    public PageResult<SystemRole> page(PageDTO pageDTO){
        IPage<SystemRole> userPage = service.getRolePage(pageDTO);
        return PageResult.of(userPage);

    }

    @ApiOperation("角色详情")
    @GetMapping("info")
    public Result<SystemRole> info(@Valid  Integer roleId){
        SystemRole info = service.getInfo(roleId);
        return Result.success(info);
    }

}
