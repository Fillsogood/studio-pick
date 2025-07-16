package org.example.studiopick.application.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDto(
    Long id,
    String writer,
    String comment,
    Short rating,
    List<String> imageUrls,
    LocalDateTime createdAt
) {}
