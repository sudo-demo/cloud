package com.example.system.controller;

import com.example.common.model.Result;
import com.example.common.service.UploadFileService;
import com.example.common.service.impl.UploadFileServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("UploadFile")//要访问这个类下所有方法，路径必须有/user
@Api(value = "上传文件",tags="上传文件")
public class UploadFileController {

    @Resource
    UploadFileService uploadFileService;

    @ApiOperation("上传图片")
    @PostMapping("/images")
    public Result images(@RequestParam("file") MultipartFile file){
        UploadFileServiceImpl.FileUploadResponse response = uploadFileService.uploadImages(file);
        return Result.success(response);
    }

    @ApiOperation("上传文件")
    @PostMapping("/files")
    public Result<Void> files(@RequestParam("file") MultipartFile file){
        System.out.println(file);


        return Result.success();
    }


}
