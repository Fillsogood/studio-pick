package org.example.studiopick.application.payment;

import org.example.studiopick.application.payment.dto.*;

import java.time.LocalDate;

/**
 * 결제 관리 서비스 인터페이스
 */
public interface PaymentService {

    /**
     * 결제 요청 (결제 정보 생성)
     */
    PaymentRequestResponse requestPayment(PaymentRequestCommand command);

    /**
     * 결제 승인
     */
    PaymentConfirmResponse confirmPayment(PaymentConfirmCommand command);

    /**
     * 결제 전 검증
     */
    void validatePaymentBeforeConfirm(String paymentKey, String orderId);

    /**
     * 결제 취소
     */
    PaymentCancelResponse cancelPayment(String paymentKey, PaymentCancelCommand command);

    /**
     * 결제 정보 조회
     */
    PaymentInfoResponse getPaymentInfo(String paymentKey);

    /**
     * 예약별 결제 정보 조회
     */
    PaymentInfoResponse getPaymentByReservation(Long reservationId);

    /**
     * 사용자별 결제 내역 조회
     */
    UserPaymentHistoryListResponse getUserPaymentHistory(Long userId, int page, int size,
                                                         String status, LocalDate startDate, LocalDate endDate);
}
