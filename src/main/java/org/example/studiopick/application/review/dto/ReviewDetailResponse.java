package org.example.studiopick.application.review.dto;

import org.example.studiopick.domain.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewDetailResponse(
    Long id,
    Long userId,
    String nickname,
    Long studioId,
    Long workshopId,
    Short rating,
    String comment,
    ReviewStatus status,
    List<String> imageUrls,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}