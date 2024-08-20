package com.example.common.service;

import com.example.common.service.impl.UploadFileServiceImpl;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {

    UploadFileServiceImpl.FileUploadResponse uploadImages(MultipartFile file);

    void uploadFiles(MultipartFile file);


}
