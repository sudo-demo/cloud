package com.example.common.service.impl;

import com.example.common.config.FileUploadConfig;
import com.example.common.service.UploadFileService;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {
    @Resource
    FileUploadConfig fileUploadConfig; // 注入文件上传配置

    @Override
    public FileUploadResponse uploadImages(MultipartFile file) {
        // 处理图片上传
        return handleFileUpload(file, fileUploadConfig.getImages());
    }

    @Override
    public void uploadFiles(MultipartFile file) {
        // TODO: 实现文件上传逻辑
    }

    /**
     * 处理上传文件
     * @param file 上传的文件
     * @param fileTypeConfig 文件类型配置
     * @return 文件上传响应
     */
    public FileUploadResponse handleFileUpload(MultipartFile file, FileUploadConfig.FileTypeConfig fileTypeConfig) {
        // 验证文件大小
        if (file.getSize() > parseSize(fileTypeConfig.getMaxSize())) {
            throw new ValidationException("文件大小超过限制");
        }

        // 验证文件类型
        if (!fileTypeConfig.getAllowedTypes().contains(file.getContentType())) {
            throw new ValidationException("文件类型不被允许");
        }

        // 获取文件后缀
        String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
        if (!fileTypeConfig.getAllowedExtensions().contains(fileExtension)) {
            throw new ValidationException("文件后缀不被允许");
        }

        // 生成唯一文件名，使用 UUID
        String uniqueFileName = UUID.randomUUID() + fileExtension; // 仅拼接文件后缀

        // 使用临时文件保存上传文件
        Path tempFilePath;
        try {
            tempFilePath = Files.createTempFile("upload-", fileExtension); // 创建临时文件
            file.transferTo(tempFilePath); // 将文件保存到临时路径
        } catch (IOException e) {
            throw new ValidationException("文件上传失败");
        }

        // 创建以当前日期命名的文件夹
        String dateFolder = new SimpleDateFormat("yyyy-MM").format(new Date());
        Path datePath = Paths.get(fileTypeConfig.getDir(), dateFolder);
        try {
            Files.createDirectories(datePath); // 创建日期目录
        } catch (IOException e) {
            throw new ValidationException("创建日期目录失败");
        }

        // 移动文件到目标目录
        Path finalPath = datePath.resolve(uniqueFileName);
        try {
            Files.move(tempFilePath, finalPath); // 移动临时文件到目标路径
        } catch (IOException e) {
            throw new ValidationException("文件上传失败");
        }

        // 返回文件上传响应
        String filePath = finalPath.toString().replace(fileUploadConfig.getUploadDir(), fileUploadConfig.getStaticAccessPath());
        return new FileUploadResponse(file.getOriginalFilename(), fileExtension, file.getContentType(), file.getSize(), filePath, fileUploadConfig.getAccessPath() + filePath);
    }

    /**
     * 解析文件大小字符串，转换为字节数
     * @param size 文件大小字符串（如 "10MB"）
     * @return 文件大小的字节数
     */
    private long parseSize(String size) {
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "").trim()) * 1024 * 1024; // 转换为字节
        }
        return Long.parseLong(size); // 默认返回字节数
    }

    /**
     * 获取文件后缀名
     * @param filename 文件名
     * @return 文件后缀名（小写）
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf('.')).toLowerCase(); // 获取文件后缀
    }

    @Data
    public static class FileUploadResponse {
        @ApiModelProperty("文件名")
        private String fileName; // 文件名
        @ApiModelProperty("文件后缀")
        private String fileExtension; // 文件后缀
        @ApiModelProperty("文件类型")
        private String fileType; // 文件类型
        @ApiModelProperty("文件大小")
        private long fileSize; // 文件大小（字节）
        @ApiModelProperty("相对路径")
        private String relativePath; // 文件相对路径
        @ApiModelProperty("访问路径")
        private String accessPath; // 文件访问路径

        // 构造函数
        public FileUploadResponse(String fileName, String fileExtension, String fileType, long fileSize, String relativePath, String accessPath) {
            this.fileName = fileName;
            this.fileExtension = fileExtension;
            this.fileType = fileType;
            this.fileSize = fileSize;
            this.relativePath = relativePath;
            this.accessPath = accessPath;
        }
    }
}
