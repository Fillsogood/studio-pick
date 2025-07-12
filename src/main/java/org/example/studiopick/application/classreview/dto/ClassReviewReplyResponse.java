package org.example.studiopick.application.classreview.dto;

import java.time.LocalDateTime;

public record ClassReviewReplyResponse(
    Long replyId,
    String content,
    String message
) {}
