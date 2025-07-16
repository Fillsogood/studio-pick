package org.example.studiopick.application.refund;

import org.example.studiopick.application.refund.dto.DailyRefundStatsDto;
import org.example.studiopick.application.reservation.dto.RefundInfo;
import org.example.studiopick.domain.refund.Refund;
import org.example.studiopick.domain.reservation.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 환불 처리 서비스 인터페이스
 */
public interface RefundService {

    /**
     * 예약 취소에 따른 환불 처리
     */
    void processRefundForReservation(Reservation reservation, RefundInfo refundInfo, String reason);

    /**
     * 사용자별 환불 내역 조회
     */
    List<Refund> getUserRefundHistory(Long userId);

    /**
     * 예약별 환불 내역 조회
     */
    List<Refund> getReservationRefundHistory(Long reservationId);

    /**
     * 일별 환불 통계 조회
     */
    List<DailyRefundStatsDto> getDailyRefundStats(LocalDate startDate, LocalDate endDate);

    /**
     * 환불 실패 건 조회 (관리자용)
     */
    List<Refund> getFailedRefunds();

    /**
     * 일반 결제 취소
     */
    void cancelPayment(String paymentKey, BigDecimal cancelAmount, String reason);
}
