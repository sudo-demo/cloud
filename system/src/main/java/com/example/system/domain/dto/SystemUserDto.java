package com.example.system.domain.dto;

import javax.validation.constraints.*;

import java.io.Serializable;


import com.example.common.constants.Add;
import com.example.common.constants.Update;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户信息表
 *
 * @TableName system_user
 */
@Data
public class SystemUserDto implements Serializable {

    /**
     * 用户ID，主键
     */
    @NotNull(message = "[用户ID，主键]不能为空", groups = {Update.class})
    @ApiModelProperty(value = "用户ID，主键", required = true, example = "11")
    private Long userId;
    /**
     * 登录账号
     */
    @NotBlank(message = "[登录账号]不能为空", groups = {Add.class})
    @Size(max = 20, message = "编码长度不能超过20")
    @ApiModelProperty("登录账号")
    @Length(max = 20, message = "编码长度不能超过20")
    private String loginId;
    /**
     * 密码
     */
    @NotBlank(message = "[密码]不能为空", groups = {Add.class})
    @Size(max = 100, message = "编码长度不能超过100", groups = {Add.class})
    @ApiModelProperty("密码")
    private String password;

    /**
     * 用户名
     */
    @NotBlank(message = "[用户名]不能为空", groups = {Add.class, Update.class})
    @Size(max = 64, message = "编码长度不能超过64", groups = {Add.class, Update.class})
    @ApiModelProperty("用户名")
    private String userName;
    /**
     * 手机
     */
    @Size(max = 20, message = "编码长度不能超过20", groups = {Add.class, Update.class})
    @ApiModelProperty("手机")
    private String phone;
    /**
     * 用户邮箱
     */
    @Size(max = 50, message = "编码长度不能超过50", groups = {Add.class, Update.class})
    @Email(groups = {Add.class})
    @ApiModelProperty("用户邮箱")
    private String email;
    /**
     * 状态 (1正常 2停用)
     */
    @ApiModelProperty("状态 (1正常 2停用)")
    @Max(value = 1, message = "状态长度不能超过1", groups = {Add.class, Update.class})
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 255, message = "编码长度不能超过255", groups = {Add.class, Update.class})
    @ApiModelProperty("备注")
    private String remark;


}
