package org.example.studiopick.application.review.dto;

import org.example.studiopick.domain.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewSummaryDto(
    Long id,
    Long userId,
    String nickname,
    Short rating,
    String comment,
    ReviewStatus status,
    LocalDateTime createdAt,
    List<String> imageUrls
) {
}