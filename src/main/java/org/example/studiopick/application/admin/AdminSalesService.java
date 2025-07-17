package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.sales.*;

/**
 * 관리자 매출 관리 서비스 인터페이스
 */
public interface AdminSalesService {

    /**
     * 전체 매출 통계 조회
     */
    AdminSalesStatsResponse getSalesStats();

    /**
     * 기간별 매출 트렌드 분석
     */
    AdminSalesTrendResponse getSalesTrend(String startDate, String endDate, String period);

    /**
     * 스튜디오별 매출 분석
     */
    AdminStudioSalesResponse getStudioSalesAnalysis(int page, int size, String startDate, String endDate);

    /**
     * 결제 방법별 통계
     */
    AdminPaymentMethodStatsResponse getPaymentMethodStats(String startDate, String endDate);

    /**
     * 환불 통계 및 분석
     */
    AdminRefundStatsResponse getRefundStats(String startDate, String endDate);

    /**
     * 매출 상세 내역 조회
     */
    AdminSalesDetailResponse getSalesDetails(int page, int size, String startDate, String endDate, String method, String status);
}
