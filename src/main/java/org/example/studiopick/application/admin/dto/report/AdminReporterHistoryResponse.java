package org.example.studiopick.application.admin.dto.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 신고자 이력 응답
 */
public record AdminReporterHistoryResponse(
        Long userId,
        String userName,
        String userEmail,
        ReporterStats stats,
        List<ReportHistoryItem> reportHistory,
        AdminPaginationInfo pagination
) {
    
    public record ReporterStats(
            long totalReports,
            long approvedReports,
            long rejectedReports,
            double approvalRate,
            LocalDateTime firstReportDate,
            LocalDateTime lastReportDate,
            boolean isMalicious
    ) {}
    
    public record ReportHistoryItem(
            Long reportId,
            String reportedType,
            Long reportedId,
            String reportedContent,
            String reason,
            String status,
            LocalDateTime reportedAt,
            LocalDateTime processedAt
    ) {}
    
    public record AdminPaginationInfo(
            int currentPage,
            long totalElements,
            int totalPages
    ) {}
}
