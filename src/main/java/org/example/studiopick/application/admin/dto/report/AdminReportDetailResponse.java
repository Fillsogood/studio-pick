package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDateTime;

/**
 * 신고 상세 응답
 */
public record AdminReportDetailResponse(
        Long id,
        ReportType reportedType,
        Long reportedId,
        String contentTitle,
        String contentImageUrl,
        ReporterInfo reporterInfo,
        ContentOwnerInfo contentOwnerInfo,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        AdminProcessInfo adminProcessInfo,
        long totalReportCount,
        long pendingReportCount
) {
    
    public record ReporterInfo(
            Long userId,
            String userName,
            String userEmail,
            long reportCount
    ) {}
    
    public record ContentOwnerInfo(
            Long userId,
            String userName,
            String userEmail,
            long reportedCount
    ) {}
    
    public record AdminProcessInfo(
            Long adminId,
            String adminName,
            String adminComment,
            LocalDateTime processedAt
    ) {}
}
