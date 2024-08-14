package com.example.system.domain.dto;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 用户授权角色数据范围
* @TableName system_user_role_scope
*/
@Data
public class SystemUserRoleScope implements Serializable {

    /**
    * 主键
    */
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty("主键")
    private Long userRoleScopeId;
    /**
    * 
    */
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("学院Id")
    @Length(max= 100,message="编码长度不能超过100")
    private String collegeId;
    /**
    * 用户授权角色表主键
    */
    @NotNull(message="[用户授权角色表主键]不能为空")
    @ApiModelProperty("用户授权角色表主键")
    private Long userRoleId;


}
