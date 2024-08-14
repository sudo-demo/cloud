package com.example.system.domain.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
* 用户授权角色表
* @TableName system_user_role
*/
@Data
@ApiModel(description = "请求体参数")
public class SystemUserRoleDto implements Serializable {

    /**
    * 用户ID
    */
    @NotNull(message="[用户ID]不能为空")
    @ApiModelProperty("用户ID")
    private Long userId;
    /**
    * 角色ID
    */
    @NotNull(message="[角色]不能为空")
    @ApiModelProperty(value = "角色",example = "1")
    @Valid //深层验证
    private List<RoleListDto> RoleList;



}
