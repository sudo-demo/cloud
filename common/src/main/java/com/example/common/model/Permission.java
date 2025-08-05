package com.example.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;


/**
 *
 */
@Data
public class Permission implements Serializable {

    private static final long serialVersionUID = 2192317564009914585L;

    @Getter
    @TableField(exist = false)//表示该字段在数据库表中不存在
    @ApiModelProperty(value = "数据权限")
    private static String dataScope = "/* DATA_SCOPE_CONDITION */";

}
