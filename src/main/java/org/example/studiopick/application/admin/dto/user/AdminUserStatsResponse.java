package org.example.studiopick.application.admin.dto.user;

public record AdminUserStatsResponse(
    long totalUsers,
    long activeUsers,
    long inactiveUsers,
    long lockedUsers,
    long regularUsers,
    long studioOwners,
    long admins
) {}
