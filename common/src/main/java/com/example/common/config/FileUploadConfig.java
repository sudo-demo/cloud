package com.example.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfig {

    private String accessPath; //访问路径
    private String staticAccessPath; //静态访问路径
    private String uploadDir; // 文件上传目录
    private FileTypeConfig images; // 图片配置
    private FileTypeConfig files; // 普通文件配置
    private FileTypeConfig xmls; // XML 文件配置

    @Data
    public static class FileTypeConfig {
        private String dir; // 上传目录
        private String maxSize; // 最大文件大小
        private List<String> allowedTypes; // 允许的文件类型
        private List<String> allowedExtensions; // 允许的文件后缀

    }

}
