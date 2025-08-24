package com.example.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文件表
 */
@ApiModel(description = "文件表")
@Data
@TableName(value = "system_file")
public class SystemFile implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 文件id
     */
    @TableField(value = "file_id")
    @ApiModelProperty(value = "文件id")
    private Integer fileId;

    /**
     * 文件名称
     */
    @TableField(value = "file_name")
    @ApiModelProperty(value = "文件名称")
    private String fileName;

    /**
     * 文件相对路径
     */
    @TableField(value = "file_path")
    @ApiModelProperty(value = "文件相对路径")
    private String filePath;

    /**
     * 文件绝对路径
     */
    @TableField(value = "file_url")
    @ApiModelProperty(value = "文件绝对路径")
    private String fileUrl;

    /**
     * 文件类型
     */
    @TableField(value = "file_type")
    @ApiModelProperty(value = "文件类型")
    private String fileType;

    /**
     * 文件大小(单位bytes)
     */
    @TableField(value = "file_size")
    @ApiModelProperty(value = "文件大小(单位bytes)")
    private String fileSize;

    /**
     * 上传时间
     */
    @TableField(value = "created_at")
    @ApiModelProperty(value = "上传时间")
    private Date createdAt;

    /**
     * 删除时间
     */
    @TableField(value = "deleted_at")
    @ApiModelProperty(value = "删除时间")
    private Date deletedAt;

}