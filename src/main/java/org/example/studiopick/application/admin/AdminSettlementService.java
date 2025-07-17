package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.settlement.SettlementDTOs.*;

/**
 * 관리자 정산 관리 서비스 인터페이스
 */
public interface AdminSettlementService {

    /**
     * 정산 대상 조회
     */
    AdminSettlementListResponse getSettlementTargets(int page, int size, String status, String startDate, String endDate);

    /**
     * 정산 상세 조회
     */
    AdminSettlementDetailResponse getSettlementDetail(Long settlementId);

    /**
     * 정산 처리
     */
    AdminSettlementProcessResponse processSettlement(Long settlementId, AdminSettlementProcessCommand command);

    /**
     * 대량 정산 처리
     */
    AdminBulkSettlementResponse processBulkSettlement(AdminBulkSettlementCommand command);

    /**
     * 정산 통계
     */
    AdminSettlementStatsResponse getSettlementStats(String startDate, String endDate);

    /**
     * 스튜디오별 정산 내역
     */
    AdminStudioSettlementResponse getStudioSettlement(Long studioId, int page, int size, String status);
    
    /**
     * 워크샵별 정산 내역
     */
    AdminWorkshopSettlementResponse getWorkshopSettlement(Long workshopId, int page, int size, String status);

    /**
     * 정산 수수료 설정 관리
     */
    AdminCommissionRateResponse getCommissionRates();
    
    AdminCommissionRateResponse updateCommissionRate(AdminCommissionRateUpdateCommand command);

    /**
     * 정산 보고서 생성
     */
    AdminSettlementReportResponse generateSettlementReport(AdminSettlementReportCommand command);

    /**
     * 정산 승인/거부
     */
    AdminSettlementApprovalResponse approveSettlement(Long settlementId, AdminSettlementApprovalCommand command);
}
