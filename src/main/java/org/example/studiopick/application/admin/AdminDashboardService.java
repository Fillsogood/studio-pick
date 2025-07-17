package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.dashboard.*;

import java.time.LocalDate;

/**
 * 관리자 대시보드 서비스 인터페이스
 */
public interface AdminDashboardService {

    /**
     * 관리자 메인 대시보드 데이터 조회
     */
    AdminDashboardResponse getDashboardData();

    /**
     * 기간별 대시보드 통계
     */
    AdminDashboardStatsResponse getDashboardStats(LocalDate startDate, LocalDate endDate);

    /**
     * 실시간 통계 조회
     */
    AdminRealTimeStatsResponse getRealTimeStats();

    /**
     * 주요 지표 요약
     */
    AdminKpiSummaryResponse getKpiSummary();
}
