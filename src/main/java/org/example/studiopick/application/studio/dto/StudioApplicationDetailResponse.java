package org.example.studiopick.application.studio.dto;

import java.time.LocalDateTime;

public record StudioApplicationDetailResponse(
    Long id,
    String studioName,
    String status,
    LocalDateTime submittedAt,
    String reviewComments
) {}
