package org.example.studiopick.application.review.dto;

public record ReviewReplyRequest(
    Long reviewId,
    String content
) {}
