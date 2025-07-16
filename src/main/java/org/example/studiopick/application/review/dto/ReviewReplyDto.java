package org.example.studiopick.application.review.dto;

import java.time.LocalDateTime;

public record ReviewReplyDto(
    Long reviewId,
    String comment,
    Short rating,
    String writer,
    String replyContent,
    LocalDateTime createdAt
) {}
