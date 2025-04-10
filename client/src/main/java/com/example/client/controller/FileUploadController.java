package com.example.client.controller;

import com.example.client.service.impl.FileUploadImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileUploadController {

    @Autowired
    private FileUploadImpl fileUploadService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        return this.fileUploadService.uploadFile(multipartFile);
    }

    @GetMapping("/download")
    public String downloadFile(@RequestParam String fileName) {
        return this.fileUploadService.downloadFile(fileName);

    }
}
