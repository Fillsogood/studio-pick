package org.example.studiopick.application.admin.dto.dashboard;

import java.time.LocalDateTime;

/**
 * 실시간 통계 응답
 */
public record AdminRealTimeStatsResponse(
    long currentActiveUsers,
    long todaySignups,
    long todayReservations,
    java.math.BigDecimal todaySales,
    PendingCounts pendingCounts,
    LocalDateTime lastUpdated
) {

    public record PendingCounts(
        long pendingReservations,
        long pendingReports,
        long pendingStudioApprovals,
        long pendingRefunds
    ) {}

    public record SystemHealth(
        String status,
        double cpuUsage,
        double memoryUsage,
        double diskUsage,
        String databaseStatus,
        long responseTime
    ) {}
}
