package org.example.studiopick.application.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(

        String type,
        Long targetId,
        Short rating,
        String comment,

        @Schema(description = "이미지 파일들", type = "string", format = "binary")
        List<MultipartFile> imageUrls
) {}

