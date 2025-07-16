package org.example.studiopick.application.payment;

import org.example.studiopick.application.payment.dto.TossPaymentCancelRequest;
import org.example.studiopick.application.payment.dto.TossPaymentCancelResponse;
import org.example.studiopick.application.payment.dto.TossPaymentResponse;

/**
 * 토스페이먼츠 API 연동 서비스 인터페이스
 */
public interface TossPaymentsService {

    /**
     * 토스페이먼츠 결제 승인 처리
     */
    TossPaymentResponse confirmPayment(String paymentKey, String orderId, Long amount);

    /**
     * PaymentKey로 결제 조회
     */
    TossPaymentResponse getPayment(String paymentKey);

    /**
     * 전액 결제 취소
     */
    TossPaymentCancelResponse cancelPayment(String paymentKey, String cancelReason);

    /**
     * 부분 결제 취소 (환불 금액 지정 가능)
     */
    TossPaymentCancelResponse cancelPaymentPartial(String paymentKey, TossPaymentCancelRequest request);
}
