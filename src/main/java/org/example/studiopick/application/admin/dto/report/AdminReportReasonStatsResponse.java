package org.example.studiopick.application.admin.dto.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고 사유별 통계 응답
 */
public record AdminReportReasonStatsResponse(
        String startDate,
        String endDate,
        List<ReasonStats> reasonStats,
        TotalStats totalStats
) {
    
    public record ReasonStats(
            String reason,
            long count,
            double percentage,
            long resolvedCount,
            double resolutionRate
    ) {}
    
    public record TotalStats(
            long totalReports,
            long uniqueReasons,
            String mostCommonReason,
            double overallResolutionRate
    ) {}
}
