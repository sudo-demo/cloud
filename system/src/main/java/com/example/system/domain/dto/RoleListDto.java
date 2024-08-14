package com.example.system.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
@ApiModel(description = "实体描述")
@Data
public class RoleListDto {

    /**
     * 角色ID
     */
    @NotNull(message="[角色ID]不能为空")
    @ApiModelProperty("角色ID")
    private Long roleId;

    /**
     *
     */
    @Size(max= 100,message="编码长度不能超过100")
    @ApiModelProperty("学院ID")
    @Length(max= 100,message="编码长度不能超过100")
    private String collegeId;
}
