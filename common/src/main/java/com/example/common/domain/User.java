package com.example.common.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 用户信息表
 *
 */
@Data
@TableName(value = "system_user")
public class User implements Serializable {

    /**
     * 用户ID，主键
     */
    private Long userId;
    /**
     * 登录账号
     */
    @TableField(value = "login_id")
    private String loginId;
    /**
     * 密码
     */
    @TableField(value = "password")
    private String password;
    /**
     * 用户类型：(100系统用户)
     */
    @TableField(value = "user_type")
    private String userType;
    /**
     * 用户名
     */
    @TableField(value = "user_name")
    private String userName;
    /**
     * 手机
     */
    @TableField(value = "phone")
    private String phone;
    /**
     * 用户邮箱
     */
    @TableField(value = "email")
    private String email;
    /**
     * 状态 (1正常 2停用)
     */
    @TableField(value = "status")
    private Integer status;
    /**
     * 最后登陆IP
     */
    @TableField(value = "login_ip")
    private String loginIp;
    /**
     * 最后登陆时间
     */
    @TableField(value = "login_time")
    private Date loginTime;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "updated_at",fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    /**
     * 删除时间
     */
    @TableLogic(value = "null", delval = "now()")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "deleted_at")
    private Date deletedAt;
    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
    /**
     * 动态口令牌
     */
    @TableField(value = "e_token")
    private String eToken;
    /**
     * 微信的 openid
     */
    @TableField(value = "wechat_openid")
    private String wechatOpenid;

    /**
     * 角色
     */
    private Set<Long> roleIds;


}
