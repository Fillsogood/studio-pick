package org.example.studiopick.application.classreview.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ClassReviewCreateRequest(
    Long classId,
    Short rating,
    String comment,
    List<MultipartFile> imageUrls
) {}
