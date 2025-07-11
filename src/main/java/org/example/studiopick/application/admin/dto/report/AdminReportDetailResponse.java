package org.example.studiopick.application.admin.dto.report;

import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

import java.time.LocalDateTime;

public record AdminReportDetailResponse(
        Long reportId,
        ReportType reportedType,
        Long reportedId,
        String reportedContent,
        String reportedImageUrl,  // 작품/클래스 이미지 등
        ReporterInfo reporter,
        ContentOwnerInfo contentOwner,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        AdminProcessInfo adminProcess,
        Long totalReportCount,  // 해당 콘텐츠의 총 신고 횟수
        Long pendingReportCount // 해당 콘텐츠의 대기 중인 신고 횟수
) {
    public record ReporterInfo(
            Long userId,
            String username,
            String email,
            Long reportCount  // 이 사용자의 총 신고 횟수
    ) {}
    
    public record ContentOwnerInfo(
            Long userId,
            String username,
            String email,
            Long reportedCount  // 이 사용자의 콘텐츠가 받은 총 신고 횟수
    ) {}
    
    public record AdminProcessInfo(
            Long adminId,
            String adminName,
            String adminComment,
            LocalDateTime processedAt
    ) {}
}