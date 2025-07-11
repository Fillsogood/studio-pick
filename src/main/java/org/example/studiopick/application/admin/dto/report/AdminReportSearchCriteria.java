package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDateTime;

public record AdminReportSearchCriteria(
        ReportType reportedType,
        ReportStatus status,
        Long reporterId,
        Long contentOwnerId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String keyword,  // 신고 사유나 콘텐츠 제목 검색
        int page,
        int size,
        String sortBy,  // createdAt, processedAt, reportCount
        String sortDirection  // asc, desc
) {
    public AdminReportSearchCriteria {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
        if (sortBy == null) sortBy = "createdAt";
        if (sortDirection == null) sortDirection = "desc";
    }
}