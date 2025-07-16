package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.report.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 신고 관리 서비스 인터페이스
 */
public interface AdminReportService {
    
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
     * 대기 중인 신고 수 조회
     */
    long getPendingReportCount();
}
