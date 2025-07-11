package org.example.studiopick.application.review.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(
    Long studioId,
    short rating,
    String comment,
    List<MultipartFile> images
) {}
