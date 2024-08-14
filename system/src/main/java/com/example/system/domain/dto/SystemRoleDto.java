package com.example.system.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 角色信息表
 * @TableName system_role
 */
@Data
@TableName(value ="system_role")
public class SystemRoleDto implements Serializable {
    /**
     * 主键
     */
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty(value = "主键",required = true)
    @TableId(value = "role_id")
    private Long roleId;

    /**
     * 角色名称
     */
    @NotBlank(message="[角色名称]不能为空")
    @Size(max= 30,message="编码长度不能超过30")
    @ApiModelProperty("角色名称")
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 状态 (1正常 2停用)
     */
    @ApiModelProperty("状态 (1正常 2停用)")
    @TableField(value = "status")
    private Integer status;

    /**
     * 排序
     */
    @ApiModelProperty("排序")
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 备注
     */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("备注")
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}