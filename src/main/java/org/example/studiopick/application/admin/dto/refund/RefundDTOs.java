package org.example.studiopick.application.admin.dto.refund;

import org.example.studiopick.domain.common.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RefundDTOs {

  public record AdminRefundListResponse(
      List<RefundSummary> refunds,
      RefundPagination pagination
  ) {
    public record RefundSummary(
        Long refundId,
        Long reservationId,
        Long paymentId,
        String userName,
        BigDecimal refundAmount,
        RefundStatus status,
        LocalDateTime requestedAt
    ) {}
  }

  public record RefundPagination(
      int currentPage,
      long totalElements,
      int totalPages
  ) {}

  public record AdminRefundDetailResponse(
      Long refundId,
      Long reservationId,
      String userName,
      String userEmail,
      Long paymentId,
      BigDecimal refundAmount,
      BigDecimal originalAmount,
      BigDecimal cancellationFee,
      String refundReason,
      String refundPolicy,
      RefundStatus status,
      String paymentKey,
      String transactionKey,
      LocalDateTime requestedAt,
      LocalDateTime refundedAt,
      String failureReason
  ) {}

  public record AdminRefundProcessCommand(
      String action,   // APPROVE, REJECT
      String reason
  ) {}

  public record AdminRefundProcessResponse(
      Long refundId,
      RefundStatus status,
      LocalDateTime processedAt
  ) {}

  public record AdminRefundStatsResponse(
      long totalRefunds,
      long pendingRefunds,
      long completedRefunds,
      long failedRefunds,
      BigDecimal totalRefundAmount
  ) {}

  public record AdminBulkRefundCommand(
      List<Long> refundIds,
      String action,
      String reason
  ) {}

  public record AdminBulkRefundResponse(
      int successCount,
      int failedCount
  ) {}

  public record AdminRefundReportCommand(
      String startDate,
      String endDate
  ) {}

  public record AdminRefundReportResponse(
      String reportUrl,
      LocalDateTime generatedAt
  ) {}
}
