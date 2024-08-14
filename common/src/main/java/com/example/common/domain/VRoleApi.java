package com.example.common.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @TableName v_role_api
 */
@Data
@TableName(value ="v_role_api")
public class VRoleApi implements Serializable {
    /**
     * 角色id
     */
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 主键
     */
    @TableField(value = "role_api_id")
    private Long roleApiId;

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
     * 主键
     */
    @TableField(value = "api_id")
    private Long apiId;

    /**
     * 父ID
     */
    @TableField(value = "parent_api_id")
    private Long parentApiId;

    /**
     * 接口名称
     */
    @TableField(value = "api_name")
    private String apiName;

    /**
     * 是否为按钮  0=>否，1=>是
     */
    @TableField(value = "is_button")
    private String isButton;

    /**
     * 后台 模块/控制器
     */
    @TableField(value = "app_model")
    private String appModel;

    /**
     * 后台 方法
     */
    @TableField(value = "action")
    private String action;

    @TableField(exist = false)
    private Map<String,VRoleApi> children = new HashMap<>();


    @TableField(exist = false)
    private static final long serialVersionUID = -3802304308941259000L;


}