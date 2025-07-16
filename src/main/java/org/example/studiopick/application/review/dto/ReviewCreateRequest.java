package org.example.studiopick.application.review.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(
    Long classId,
    Short rating,
    String comment,
    List<MultipartFile> imageUrls
) {}
