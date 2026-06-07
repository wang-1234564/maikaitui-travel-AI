package com.maikaitui.file.controller;

import com.maikaitui.common.core.Result;
import com.maikaitui.file.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        return fileService.uploadFile(file);
    }

    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        fileService.downloadFile(fileName, response);
    }

    @DeleteMapping("/{fileName}")
    public Result delete(@PathVariable String fileName) {
        return fileService.deleteFile(fileName);
    }
}
