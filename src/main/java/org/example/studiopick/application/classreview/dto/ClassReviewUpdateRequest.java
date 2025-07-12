package org.example.studiopick.application.classreview.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ClassReviewUpdateRequest(
    Short rating,
    String comment,
    MultipartFile imageUrl
) {}
