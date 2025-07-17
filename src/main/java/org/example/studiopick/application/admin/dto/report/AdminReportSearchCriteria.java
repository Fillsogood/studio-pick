package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

/**
 * 신고 검색 조건
 */
public record AdminReportSearchCriteria(
        ReportStatus status,
        ReportType reportedType,
        String startDate,
        String endDate,
        String reason,
        Long reporterId,
        Long contentId,
        String keyword,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {
    
    public AdminReportSearchCriteria {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        if (sortBy == null || sortBy.trim().isEmpty()) sortBy = "createdAt";
        if (sortDirection == null || (!sortDirection.equals("asc") && !sortDirection.equals("desc"))) {
            sortDirection = "desc";
        }
    }
    
    public static AdminReportSearchCriteria of(
            String status, String reportedType, String startDate, String endDate,
            String reason, Long reporterId, Long contentId, String keyword,
            int page, int size, String sortBy, String sortDirection) {
        
        ReportStatus reportStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                reportStatus = ReportStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        ReportType type = null;
        if (reportedType != null && !reportedType.trim().isEmpty()) {
            try {
                type = ReportType.valueOf(reportedType.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        return new AdminReportSearchCriteria(
            reportStatus, type, startDate, endDate, reason,
            reporterId, contentId, keyword, page, size, sortBy, sortDirection
        );
    }
}
