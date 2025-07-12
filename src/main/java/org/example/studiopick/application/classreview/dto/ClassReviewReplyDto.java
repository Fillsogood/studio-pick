package org.example.studiopick.application.classreview.dto;

import java.time.LocalDateTime;

public record ClassReviewReplyDto(
    Long reviewId,
    String comment,
    Short rating,
    String writer,
    String replyContent,
    LocalDateTime createdAt
) {}
