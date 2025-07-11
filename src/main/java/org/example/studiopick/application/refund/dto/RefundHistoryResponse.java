package org.example.studiopick.application.refund.dto;

import org.example.studiopick.domain.common.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환불 내역 응답 DTO
 */
public record RefundHistoryResponse(
    Long refundId,
    Long reservationId,
    Long paymentId,
    BigDecimal refundAmount,        // 실제 환불 금액
    BigDecimal originalAmount,      // 원래 결제 금액
    BigDecimal cancellationFee,     // 취소 수수료
    String refundReason,            // 환불 사유
    String refundPolicy,            // 적용된 환불 정책
    RefundStatus status,            // 환불 상태
    String tossPaymentKey,          // 토스 결제 키
    String tossTransactionKey,      // 토스 거래 키
    LocalDateTime refundedAt,       // 환불 완료 시간
    String failureReason,           // 환불 실패 사유
    LocalDateTime createdAt,        // 환불 요청 시간
    
    // 추가 계산 필드
    BigDecimal refundRate,          // 환불율 (%)
    boolean isFullRefund,           // 전액 환불 여부
    boolean isPartialRefund,        // 부분 환불 여부
    
    // 예약 정보
    ReservationInfo reservation
) {
    
    public record ReservationInfo(
        Long reservationId,
        String studioName,
        java.time.LocalDate reservationDate,
        java.time.LocalTime startTime,
        java.time.LocalTime endTime,
        Short peopleCount
    ) {}
    
    /**
     * Refund 엔티티로부터 응답 DTO 생성
     */
    public static RefundHistoryResponse from(org.example.studiopick.domain.refund.Refund refund) {
        return new RefundHistoryResponse(
            refund.getId(),
            refund.getReservation().getId(),
            refund.getPayment().getId(),
            refund.getRefundAmount(),
            refund.getOriginalAmount(),
            refund.getCancellationFee(),
            refund.getRefundReason(),
            refund.getRefundPolicy(),
            refund.getStatus(),
            refund.getTossPaymentKey(),
            refund.getTossTransactionKey(),
            refund.getRefundedAt(),
            refund.getFailureReason(),
            refund.getCreatedAt(),
            
            // 계산 필드
            refund.getRefundRate(),
            refund.isFullRefund(),
            refund.isPartialRefund(),
            
            // 예약 정보
            new ReservationInfo(
                refund.getReservation().getId(),
                refund.getReservation().getStudio().getName(),
                refund.getReservation().getReservationDate(),
                refund.getReservation().getStartTime(),
                refund.getReservation().getEndTime(),
                refund.getReservation().getPeopleCount()
            )
        );
    }
}
