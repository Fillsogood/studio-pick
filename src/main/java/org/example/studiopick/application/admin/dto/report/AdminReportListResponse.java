package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDateTime;

/**
 * 신고 목록 응답
 */
public record AdminReportListResponse(
        Long id,
        ReportType reportedType,
        Long reportedId,
        String reportedContent,
        Long reporterId,
        String reporterName,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        Long adminId,
        String adminName
) {}
