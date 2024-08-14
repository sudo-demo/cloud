package com.example.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色与接口关联表(权限)
 * @TableName system_role_api
 */
@Data
@TableName(value ="system_role_api")
public class SystemRoleApi implements Serializable {
    /**
     * 主键
     */
    @TableField(value = "role_api_id")
    private Long roleApiId;

    /**
     * 接口id
     */
    @TableField(value = "api_id")
    private Long apiId;

    /**
     * 数据范围
     */
    @TableField(value = "data_scope")
    private String dataScope;

    /**
     * 操作状态
     */
    @TableField(value = "operating_status")
    private String operatingStatus;

    /**
     * 数据字段
     */
    @TableField(value = "data_key")
    private String dataKey;

    /**
     * 角色id
     */
    @TableField(value = "role_id")
    private Long roleId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}