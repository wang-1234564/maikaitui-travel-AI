package com.maikaitui.file.service;

import com.maikaitui.common.core.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Result uploadFile(MultipartFile file);
    void downloadFile(String fileName, HttpServletResponse response);
    Result deleteFile(String fileName);
}
