package com.example.system.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

/**
* 角色与接口关联表(权限)
* @TableName system_role_api
*/
public class SystemRoleApiDto implements Serializable {

    /**
    * 主键
    */
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty("主键")
    private Long roleApiId;
    /**
    * 接口id
    */
    @NotNull(message="[接口id]不能为空")
    @ApiModelProperty("接口id")
    private Long apiId;
    /**
    * 数据范围
    */
    @NotBlank(message="[数据范围]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("数据范围")
    @Length(max= 100,message="编码长度不能超过100")
    private String dataScope;
    /**
    * 操作状态
    */
    @NotBlank(message="[操作状态]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("操作状态")
    @Length(max= 100,message="编码长度不能超过100")
    private String operatingStatus;
    /**
    * 数据字段
    */
    @NotBlank(message="[数据字段]不能为空")
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("数据字段")
    @Length(max= 100,message="编码长度不能超过100")
    private String dataKey;
    /**
    * 角色id
    */
    @NotNull(message="[角色id]不能为空")
    @ApiModelProperty("角色id")
    private Long roleId;


}
