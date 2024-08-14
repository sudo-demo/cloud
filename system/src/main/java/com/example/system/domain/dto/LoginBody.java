package com.example.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("登录")
public class LoginBody {

    /**
     * 登录账号
     */
    @ApiModelProperty("登录账号")
    @NotBlank(message = "登录账号不能为空")
    private String loginId;

    /**
     * 密码
     */
    @ApiModelProperty("密码")
    @NotBlank(message = "密码不能为空")
    private String password;

}
