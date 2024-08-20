package com.example.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统日志表
 */
@ApiModel(description = "系统日志表")
@Accessors( chain = true )
@Data
@TableName(value = "system_log")
public class SystemLog implements Serializable {

    private static final long serialVersionUID = 3554367268241233891L;

    /**
     * 日志主键
     */
    @TableField(value = "log_id")
    @ApiModelProperty(value = "日志主键")
    private Long logId;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField(value = "username")
    @ApiModelProperty(value = "用户名")
    private String username;

    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    /**
     * 角色id
     */
    @TableField(value = "role_id")
    @ApiModelProperty(value = "角色id")
    private Long roleId;

    /**
     * ip地址
     */
    @TableField(value = "ip")
    @ApiModelProperty(value = "ip地址")
    private String ip;

    /**
     * 请求参数
     */
    @TableField(value = "request")
    @ApiModelProperty(value = "请求参数")
    private String request;

    /**
     * 相应数据
     */
    @TableField(value = "response")
    @ApiModelProperty(value = "相应数据")
    private String response;

    /**
     * 响应状态码
     */
    @TableField(value = "code")
    @ApiModelProperty(value = "响应状态码")
    private String code;

    /**
     * 响应提示信息
     */
    @TableField(value = "response_message")
    @ApiModelProperty(value = "响应提示信息")
    private String responseMessage;

    /**
     * 接口地址
     */
    @TableField(value = "api_url")
    @ApiModelProperty(value = "接口地址")
    private String apiUrl;

    /**
     * 接口名称
     */
    @TableField(value = "api_name")
    @ApiModelProperty(value = "接口名称")
    private String apiName;

    /**
     * 操作时间
     */
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    @ApiModelProperty(value = "操作时间")
    private Date createdAt;

}