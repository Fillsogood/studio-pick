package org.example.studiopick.application.review.dto;

public record ReviewReplyResponse(
    Long replyId,
    String content,
    String message
) {}
