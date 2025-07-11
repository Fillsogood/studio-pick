package org.example.studiopick.application.review.dto;

import java.time.LocalDateTime;

// 리뷰 목록에서 답글 포함 조회용
public record ReviewWithReplyDto(
    Long reviewId,
    String content,
    Short rating,
    String writer,
    String replyContent, // null 가능
    LocalDateTime createdAt
) {}
