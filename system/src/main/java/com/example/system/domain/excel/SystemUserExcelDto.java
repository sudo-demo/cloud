package com.example.system.domain.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SystemUserExcelDto implements Serializable {

    private static final long serialVersionUID = -6707489683380061720L;

    /**
     * 用户ID，主键
     */
    @ExcelProperty(value = "用户ID", index = 0)
    @ColumnWidth(15)  // 设置宽度为15个字符
    private Long userId;
    /**
     * 登录账号
     */
    @ExcelProperty(value = "登录账号", index = 1)
    private String loginId;
    /**
     * 密码
     */
    @ExcelProperty(value = "密码", index = 2)
    private String password;
    /**
     * 用户类型：(100系统用户)
     */
    @ExcelProperty(value = "用户类型", index = 3)
    private String userType;
    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名", index = 4)
    private String userName;
    /**
     * 手机
     */
    @ExcelProperty(value = "手机", index = 5)
    private String phone;
    /**
     * 用户邮箱
     *  updateStrategy = FieldStrategy.IGNORED  忽略判断  可修改为null
     */
    @ExcelProperty(value = "用户邮箱", index = 6)
    private String email;
    /**
     * 状态 (1正常 2停用)
     */
    @ExcelProperty(value = "状态", index = 7)
    private Integer status;
    /**

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间", index = 8)
    private Date createdAt;

}
