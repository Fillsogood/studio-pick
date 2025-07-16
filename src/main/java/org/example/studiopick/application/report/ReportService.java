package org.example.studiopick.application.report;

import org.example.studiopick.application.report.dto.ReportRequestDto;
import org.example.studiopick.application.report.dto.ReportResponseDto;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReportType;

/**
 * 신고 관리 서비스 인터페이스
 */
public interface ReportService {

    /**
     * 신고 생성 + 자동 비공개 처리
     */
    ReportResponseDto createReport(Long userId, ReportRequestDto request);

    /**
     * 신고 처리 (관리자)
     */
    void processReport(Long reportId, Long adminId, ReportStatus status, String adminComment);

    /**
     * 신고 상태별 카운트 조회
     */
    long getReportCount(ReportType reportType, Long reportedId, ReportStatus status);

    /**
     * 총 신고 카운트 조회
     */
    long getTotalReportCount(ReportType reportType, Long reportedId);

    /**
     * 자동 비공개 처리
     */
    boolean autoHideContent(ReportType reportType, Long reportedId);

    /**
     * 비공개된 컨텐츠 복원
     */
    void restoreContent(ReportType reportType, Long reportedId);
}
