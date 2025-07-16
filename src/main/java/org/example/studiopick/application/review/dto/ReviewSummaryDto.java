package org.example.studiopick.application.review.dto;

import org.example.studiopick.domain.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ReviewSummaryDto(
    Long id,
    Long userId,
    String nickname,
    Short rating,
    String comment,
    ReviewStatus status,
    LocalDateTime createdAt
) {
}