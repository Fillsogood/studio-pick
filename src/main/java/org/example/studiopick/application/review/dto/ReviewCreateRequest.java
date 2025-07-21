package org.example.studiopick.application.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(

        String type,
        Long targetId,
        Short rating,
        String comment,

        List<String> imageUrls   // S3 URL 리스트만 받음!
) {}

