package com.example.client.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileUpload {
    String uploadFile(final MultipartFile multipartFile);
}
