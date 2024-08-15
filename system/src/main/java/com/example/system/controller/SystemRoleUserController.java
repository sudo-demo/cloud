package com.example.system.controller;


import com.example.common.domain.VRoleApi;
import com.example.system.domain.dto.SystemUserRoleDto;
import com.example.system.service.SystemUserRoleService;
import com.example.common.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;

/**
 * 
 */
@RestController//@RestController默认情况下，@RestController注解会将返回的赌侠ing数据转换为JSON格式，这样方便前端用`。
@RequestMapping("roleUser")//要访问这个类下所有方法，路径必须有/user
@Api(value="授权管理",tags = "授权管理")
public class SystemRoleUserController {


    @Resource
    private SystemUserRoleService service;

    @ApiOperation("授权角色")
    @PostMapping("authorized")
    public Result<Void> authorized(@Validated @RequestBody SystemUserRoleDto dto){
        service.authorized(dto);
        return Result.success();
    }

    @Data
    public static class RoleIdDto{
        @NotEmpty
        private Set<Long> roleId;

    }

    @ApiOperation("缓存角色")
    @PostMapping(value = {"cacheRole"})
    public Result<List<VRoleApi>> cacheRole(@RequestBody @Valid RoleIdDto roleIdDto){

        List<VRoleApi> vRoleApis = service.cacheRole(roleIdDto.getRoleId());

        return Result.success(vRoleApis);
    }


}
