package com.example.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author huanghongjia
 */
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 2192317564009914585L;

    @ApiModelProperty(value = "数据权限")
    private String dataScope = "@DataScope";

}
