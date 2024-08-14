package com.example.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色信息表
 * @author huanghongjia
 * @TableName system_role
 */
@Data
@TableName(value ="system_role")
public class SystemRole implements Serializable {
    /**
     * 主键
     */
    @NotNull(message="[主键]不能为空")
    @ApiModelProperty("主键")
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
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @TableField(value = "updated_at",fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;

    /**
     * 删除时间
     */
//    @TableLogic(value = "null", delval = "now()")
    @ApiModelProperty("删除时间")
    @TableField(value = "deleted_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deletedAt;

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