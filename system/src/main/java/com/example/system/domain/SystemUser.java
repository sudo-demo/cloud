package com.example.system.domain;

import java.io.Serializable;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.example.common.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户信息表
 *
 * @TableName system_user
 */
@ApiModel("用户")
@Data
@TableName(value = "system_user")
public class SystemUser extends BaseEntity {
//    public class SystemUser implements Serializable {

    /**
     * 用户ID，主键
     */
    @ApiModelProperty("用户id")
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
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
     *  updateStrategy = FieldStrategy.IGNORED  忽略判断  可修改为null
     */
    @TableField(value = "email",updateStrategy = FieldStrategy.IGNORED)
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
     * @TableLogic 注解可以应用于实体类的字段上，它告诉 MyBatis-Plus 这个字段是逻辑删除字段。以下是 @TableLogic 注解的一些常用属性：
     *      value: 表示未删除的记录在数据库中的值，默认为 null。
     *      delval: 表示已删除的记录在数据库中的值，默认为 当前时间。
     * @JsonFormat 是 Jackson 库中用于指定 JSON 序列化和反序列化时日期时间格式的注解。当你使用 Jackson 进行 JSON 序列化或反序列化操作时，这个注解可以帮助你控制日期时间对象的格式。
     *
     * 以下是 @JsonFormat 注解的一些常用属性：
     *
     *      pattern: 用于指定日期时间的格式模式。这个属性对应于 Java 的 SimpleDateFormat 类的模式字符串。例如，"yyyy-MM-dd HH:mm:ss" 表示年-月-日 时：分：秒。
     *      timezone: 用于指定时区，例如 "GMT" 或 "UTC"。如果不指定，默认使用系统默认时区。
     *      locale: 用于指定地区，例如 Locale.US 或 Locale.JAPAN。这可以影响某些日期时间格式的显示。
     *      shape: 用于指定序列化行为，例如 JsonFormat.Shape.STRING 表示将日期时间序列化为字符串，JsonFormat.Shape.NUMBER 表示序列化为时间戳（毫秒）
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


}
