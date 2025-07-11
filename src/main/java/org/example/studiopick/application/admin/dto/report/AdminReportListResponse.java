package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDateTime;

public record AdminReportListResponse(
        Long reportId,
        ReportType reportedType,
        Long reportedId,
        String reportedContent,  // 신고된 콘텐츠 제목/내용 일부
        Long reporterId,
        String reporterName,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        Long adminId,
        String adminName
) {}