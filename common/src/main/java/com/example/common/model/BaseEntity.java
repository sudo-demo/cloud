package com.example.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * 
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 2192317564009914585L;

    @TableField(exist = false)//表示该字段在数据库表中不存在
    @ApiModelProperty(value = "数据权限")
    private String dataScope = "@DataScope";

}
