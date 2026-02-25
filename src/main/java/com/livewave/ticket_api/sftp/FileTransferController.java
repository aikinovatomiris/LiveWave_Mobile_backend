package com.livewave.ticket_api.sftp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/files")
public class FileTransferController {

    private final SftpService sftpService;

    @Value("${file.upload.max-size}")
    private long maxSize;

    @Value("${file.upload.allowed-ext}")
    private String allowedExt;

    public FileTransferController(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    // ✅ UPLOAD (SFTP)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Файл пустой"));
        }

        if (file.getSize() > maxSize) {
            return ResponseEntity.badRequest().body(Map.of("message", "Файл слишком большой"));
        }

        String original = Objects.requireNonNull(file.getOriginalFilename(), "file");
        String cleanName = Paths.get(original).getFileName().toString(); // защита от ../
        String ext = getExt(cleanName);

        Set<String> whitelist = new HashSet<>();
        for (String e : allowedExt.split(",")) {
            whitelist.add(e.trim().toLowerCase());
        }

        if (ext.isEmpty() || !whitelist.contains(ext.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Запрещённый тип файла"));
        }

        // уникальное имя чтобы не перезатирать
        String safeName = UUID.randomUUID() + "_" + cleanName;

        String remotePath = sftpService.upload(safeName, file.getInputStream());
        return ResponseEntity.ok(Map.of(
                "message", "Uploaded",
                "remotePath", remotePath,
                "filename", safeName
        ));
    }

    // ✅ DOWNLOAD (SFTP)
    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("path") String remotePath) throws Exception {
        if (remotePath == null || remotePath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "path обязателен"));
        }

        var resource = sftpService.download(remotePath);

        String filename = StringUtils.getFilename(remotePath);
        if (filename == null) filename = "file";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }
}