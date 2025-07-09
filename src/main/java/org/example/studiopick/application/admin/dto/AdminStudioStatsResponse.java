package org.example.studiopick.application.admin.dto;

public record AdminStudioStatsResponse(
    long totalStudios,
    long activeStudios,
    long pendingStudios,
    long suspendedStudios
) {}