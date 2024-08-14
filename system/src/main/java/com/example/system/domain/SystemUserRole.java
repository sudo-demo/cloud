package com.example.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户授权角色表
 * @TableName system_user_role
 */
@Data
@TableName(value ="system_user_role")
public class SystemUserRole implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "user_role_id", type = IdType.AUTO)
    private Long userRoleId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @TableField(value = "role_id")
    private Long roleId;

    @TableField(exist = false)
    private Set<String> collegeId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}