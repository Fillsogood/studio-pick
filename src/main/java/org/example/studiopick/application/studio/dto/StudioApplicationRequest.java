package org.example.studiopick.application.studio.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record StudioApplicationRequest(
    String name,
    String description,
    String location,
    String phone,
    MultipartFile businessLicense,
    List<MultipartFile> documents,
    List<MultipartFile> images
) {}
