package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.report.*;
import org.example.studiopick.domain.common.enums.ReportType;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 신고 관리 서비스 인터페이스
 */
public interface AdminReportService {
    
    /**
     * 신고 목록 조회 (검색 조건 및 페이징 지원)
     */
    Page<AdminReportListResponse> getReportList(AdminReportSearchCriteria criteria);
    
    /**
     * 신고 상세 조회
     */
    AdminReportDetailResponse getReportDetail(Long reportId);
    
    /**
     * 신고 처리 (승인/거부/복원 등)
     */
    void processReport(Long reportId, Long adminId, AdminReportProcessCommand command);
    
    /**
     * 여러 신고 일괄 처리
     */
    void processBatchReports(List<Long> reportIds, Long adminId, AdminReportProcessCommand command);
    
    /**
     * 신고 통계 조회
     */
    AdminReportStatsResponse getReportStats(LocalDate startDate, LocalDate endDate);
    
    /**
     * 특정 콘텐츠의 모든 신고 조회
     */
    List<AdminReportListResponse> getContentReports(ReportType type, Long contentId);
    
    /**
     * 자동 비공개된 콘텐츠 목록 조회
     */
    List<AdminReportListResponse> getAutoHiddenReports();
    
    /**
     * 대기 중인 신고 수 조회
     */
    long getPendingReportCount();
    
    /**
     * 신고 사유별 통계
     */
    AdminReportReasonStatsResponse getReportReasonStats(LocalDate startDate, LocalDate endDate);
    
    /**
     * 신고자별 신고 이력 조회
     */
    AdminReporterHistoryResponse getReporterHistory(Long userId, int page, int size);
    
    /**
     * 악성 신고자 관리
     */
    AdminMaliciousReporterListResponse getMaliciousReporters(int page, int size);
    
    void blockReporter(Long userId, String reason);
}
