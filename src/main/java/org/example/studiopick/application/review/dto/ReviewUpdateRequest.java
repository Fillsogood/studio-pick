package org.example.studiopick.application.review.dto;

import org.springframework.web.multipart.MultipartFile;

public record ReviewUpdateRequest(
    Short rating,
    String comment,
    MultipartFile imageUrl
) {}
