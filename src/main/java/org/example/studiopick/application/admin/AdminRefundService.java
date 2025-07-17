package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.refund.RefundDTOs.*;

import java.util.List;

/**
 * 관리자 환불 관리 서비스 인터페이스
 */
public interface AdminRefundService {

  /**
   * 환불 요청 목록 조회 (페이징, 필터링)
   */
  AdminRefundListResponse getRefunds(int page, int size, String status, String startDate, String endDate);

  /**
   * 환불 상세 조회
   */
  AdminRefundDetailResponse getRefundDetail(Long refundId);

  /**
   * 환불 처리 (성공/실패 수동 처리)
   */
  AdminRefundProcessResponse processRefund(Long refundId, AdminRefundProcessCommand command);

  /**
   * 환불 통계 조회
   */
  AdminRefundStatsResponse getRefundStats(String startDate, String endDate);

  /**
   * 환불 요청 대기 건수 조회
   */
  long getPendingRefundCount();

  /**
   * 대량 환불 처리 (선택된 환불 ID들)
   */
  AdminBulkRefundResponse processBulkRefunds(AdminBulkRefundCommand command);

  /**
   * 환불 취소 처리 (관리자 강제)
   */
  void cancelRefund(Long refundId, String reason);
}
