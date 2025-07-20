package org.example.studiopick.application.admin.dto.studio;

public record AdminStudioStatsResponse(
    long totalStudios,
    long approvedStudios,
    long pendingStudios,
    long rejectedStudios,
    long suspendedStudios
) {}