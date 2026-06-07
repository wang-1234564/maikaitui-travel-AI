package com.maikaitui.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.maikaitui.common.core.Result;
import com.maikaitui.file.config.FileUploadConfig;
import com.maikaitui.file.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final FileUploadConfig config;
    private OSS ossClient;

    public FileServiceImpl(FileUploadConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(
                "https://" + config.getEndpoint(),
                config.getAccessKeyId(),
                config.getAccessKeySecret());
        log.info("OSS client initialized, bucket: {}", config.getBucketName());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public Result uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return Result.error("文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        String allowedExtensions = config.getAllowedExtensions();
        if (allowedExtensions != null && !allowedExtensions.isBlank()) {
            String[] allowedArray = allowedExtensions.split(",");
            final String fileExtension = extension;
            boolean allowed = Arrays.stream(allowedArray)
                    .map(String::trim)
                    .anyMatch(ext -> ext.equalsIgnoreCase(fileExtension));
            if (!allowed) {
                return Result.error("不支持的文件类型: " + extension);
            }
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 文件名
            String objectKey = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;


            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            PutObjectRequest putRequest = new PutObjectRequest(
                    config.getBucketName(), objectKey, inputStream, metadata);
            ossClient.putObject(putRequest);

            String fileUrl = "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + objectKey;

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("fileName", objectKey);
            resultMap.put("originalName", originalFilename);
            resultMap.put("fileUrl", fileUrl);
            resultMap.put("fileSize", file.getSize());

            log.info("文件上传到OSS成功: {} -> {}", originalFilename, fileUrl);
            return Result.success("上传成功", resultMap);
        } catch (IOException e) {
            log.error("文件上传到OSS失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadFile(String fileName, HttpServletResponse response) {
        if (fileName == null || fileName.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // 重定向到 OSS 公网 URL
        String ossUrl = "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + fileName;
        try {
            response.sendRedirect(ossUrl);
        } catch (IOException e) {
            log.error("下载重定向失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Result.error("文件名不能为空");
        }

        try {
            boolean exists = ossClient.doesObjectExist(config.getBucketName(), fileName);
            if (!exists) {
                return Result.error("文件不存在");
            }

            ossClient.deleteObject(config.getBucketName(), fileName);
            log.info("OSS文件删除成功: {}", fileName);
            return Result.success("文件删除成功");
        } catch (Exception e) {
            log.error("OSS文件删除失败: {}", e.getMessage(), e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
