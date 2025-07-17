package org.example.studiopick.application.admin.dto.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 악성 신고자 목록 응답
 */
public record AdminMaliciousReporterListResponse(
        List<MaliciousReporter> reporters,
        AdminPaginationInfo pagination,
        DetectionCriteria criteria
) {
    
    public record MaliciousReporter(
            Long userId,
            String userName,
            String userEmail,
            long totalReports,
            long approvedReports,
            long rejectedReports,
            double approvalRate,
            LocalDateTime lastReportDate,
            String riskLevel, // LOW, MEDIUM, HIGH, CRITICAL
            boolean isBlocked
    ) {}

    
    public record DetectionCriteria(
            double minRejectionRate,
            int minTotalReports,
            int maxReportsPerDay,
            double minMaliciousScore
    ) {}
    
    public record AdminPaginationInfo(
            int currentPage,
            long totalElements,
            int totalPages
    ) {}
}
