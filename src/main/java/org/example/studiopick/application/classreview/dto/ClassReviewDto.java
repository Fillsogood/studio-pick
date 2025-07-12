package org.example.studiopick.application.classreview.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClassReviewDto(
    Long id,
    String writer,
    String comment,
    Short rating,
    List<String> imageUrls,
    LocalDateTime createdAt
) {}
