package org.example.studiopick.application.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.payment.TossPaymentsService;
import org.example.studiopick.application.payment.dto.TossPaymentCancelRequest;
import org.example.studiopick.application.payment.dto.TossPaymentCancelResponse;
import org.example.studiopick.application.refund.dto.DailyRefundStatsDto;
import org.example.studiopick.application.reservation.dto.RefundInfo;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.common.enums.RefundStatus;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.refund.Refund;
import org.example.studiopick.infrastructure.refund.RefundRepository;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ✅ 개선된 환불 처리 서비스
 * - 환불 내역을 DB에 저장하여 이력 추적
 * - 환불 상태 관리 및 실패 처리 개선
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final JpaPaymentRepository paymentRepository;
    private final RefundRepository refundRepository;  // ✅ 환불 내역 Repository 추가
    private final TossPaymentsService tossPaymentsService;

    /**
     * ✅ 예약 취소에 따른 환불 처리 (별도 트랜잭션) - 환불 내역 DB 저장
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRefundForReservation(Reservation reservation, RefundInfo refundInfo, String reason) {
        // 1. 예약에 연결된 결제 정보 조회
        Payment payment = paymentRepository.findByReservationId(reservation.getId())
            .orElseThrow(() -> new IllegalArgumentException("해당 예약의 결제 정보를 찾을 수 없습니다."));
        
        log.info("환불 처리 시작: reservationId={}, refundAmount={}, paymentStatus={}", 
            reservation.getId(), refundInfo.refundAmount(), payment.getStatus());

        // 2. 결제 상태 확인 (취소 가능 상태인지 확인)
        if (!payment.isCancellable()) {
            throw new IllegalStateException("환불할 수 없는 결제 상태입니다: " + payment.getStatus());
        }

        // 3. PaymentKey 확인
        if (payment.getPaymentKey() == null || payment.getPaymentKey().trim().isEmpty()) {
            throw new IllegalStateException("결제 키가 없어 환불할 수 없습니다.");
        }

        // 4. ✅ 환불 내역 생성 및 저장 (PENDING 상태)
        Refund refund = Refund.builder()
            .payment(payment)
            .reservation(reservation)
            .refundAmount(refundInfo.refundAmount())
            .originalAmount(refundInfo.originalAmount())
            .cancellationFee(refundInfo.cancellationFee())
            .refundReason(reason)
            .refundPolicy(refundInfo.policy())
            .status(RefundStatus.PENDING)
            .tossPaymentKey(payment.getPaymentKey())
            .build();
        
        Refund savedRefund = refundRepository.save(refund);
        log.info("환불 내역 생성: refundId={}, status={}", savedRefund.getId(), savedRefund.getStatus());

        try {
            // 5. 환불 처리 시작 상태로 변경
            savedRefund.markAsProcessing();
            refundRepository.save(savedRefund);
            
            // 6. 토스페이먼츠 부분 취소 호출
            TossPaymentCancelRequest cancelRequest = new TossPaymentCancelRequest(
                "예약 취소: " + reason,
                refundInfo.refundAmount().longValue()
            );
            
            TossPaymentCancelResponse tossResponse = tossPaymentsService.cancelPaymentPartial(
                payment.getPaymentKey(), cancelRequest);

            // 7. ✅ 토스 응답에서 거래 키 추출 및 환불 완료 처리
            String transactionKey = extractTransactionKey(tossResponse);
            savedRefund.markAsCompleted(transactionKey);
            refundRepository.save(savedRefund);

            // 8. 결제 상태 업데이트
            if (refundInfo.refundAmount().compareTo(payment.getAmount()) >= 0) {
                payment.cancel(); // 전액 취소
            } else {
                payment.partialCancel(refundInfo.refundAmount()); // 부분 취소
            }
            
            paymentRepository.save(payment);

            log.info("환불 처리 완료: reservationId={}, refundId={}, refundAmount={}, fee={}", 
                reservation.getId(), savedRefund.getId(), refundInfo.refundAmount(), refundInfo.cancellationFee());
                
        } catch (Exception e) {
            // ✅ 환불 실패 시 상태 업데이트 및 상세 오류 정보 저장
            savedRefund.markAsFailed(e.getMessage());
            refundRepository.save(savedRefund);
            
            log.error("환불 처리 실패: reservationId={}, refundId={}, error={}", 
                reservation.getId(), savedRefund.getId(), e.getMessage());
            throw new RuntimeException("환불 처리에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ 토스페이먼츠 응답에서 거래 키 추출 (우선순위에 따라)
     */
    private String extractTransactionKey(TossPaymentCancelResponse tossResponse) {
        if (tossResponse == null) {
            log.warn("토스페이먼츠 응답이 null입니다.");
            return null;
        }
        
        // 1순위: 최상위 레벨의 transactionKey 확인
        if (tossResponse.transactionKey() != null && !tossResponse.transactionKey().trim().isEmpty()) {
            log.debug("최상위 레벨에서 transactionKey 추출: {}", tossResponse.transactionKey());
            return tossResponse.transactionKey();
        }
        
        // 2순위: cancels 배열에서 추출 (가장 최근 취소 내역)
        if (tossResponse.cancels() != null && !tossResponse.cancels().isEmpty()) {
            // 가장 마지막 취소 내역에서 transactionKey 추출
            String transactionKey = tossResponse.cancels().get(tossResponse.cancels().size() - 1).transactionKey();
            if (transactionKey != null && !transactionKey.trim().isEmpty()) {
                log.debug("cancels 배열에서 transactionKey 추출: {}", transactionKey);
                return transactionKey;
            }
        }
        
        log.warn("토스페이먼츠 응답에서 transactionKey를 찾을 수 없습니다: paymentKey={}, cancelsSize={}", 
            tossResponse.paymentKey(), 
            tossResponse.cancels() != null ? tossResponse.cancels().size() : 0);
        return null;
    }

    /**
     * ✅ 사용자별 환불 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Refund> getUserRefundHistory(Long userId) {
        return refundRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * ✅ 예약별 환불 내역 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<Refund> getReservationRefundHistory(Long reservationId) {
        return refundRepository.findByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    /**
     * ✅ 일별 환불 통계 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<DailyRefundStatsDto> getDailyRefundStats(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = refundRepository.getDailyRefundStats(startDate, endDate);
        return results.stream()
            .map(DailyRefundStatsDto::from)
            .toList();
    }

    /**
     * ✅ 환불 실패 건 조회 (관리자용)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Refund> getFailedRefunds() {
        return refundRepository.findByStatusOrderByCreatedAtDesc(RefundStatus.FAILED);
    }

    /**
     * 일반 결제 취소 (기존 PaymentService 로직)
     */
    @Override
    @Transactional
    public void cancelPayment(String paymentKey, BigDecimal cancelAmount, String reason) {
        try {
            // 1. 결제 정보 조회
            Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

            // 2. 결제 상태 확인 (취소 가능 상태인지 확인)
            if (!payment.isCancellable()) {
                throw new IllegalStateException("취소할 수 없는 결제 상태입니다: " + payment.getStatus());
            }

            // 3. 토스페이먼츠 취소 호출
            TossPaymentCancelRequest cancelRequest = new TossPaymentCancelRequest(
                reason,
                cancelAmount.longValue()
            );
            
            tossPaymentsService.cancelPaymentPartial(paymentKey, cancelRequest);

            // 4. 결제 상태 업데이트
            if (cancelAmount.compareTo(payment.getAmount()) >= 0) {
                payment.cancel();
            } else {
                payment.partialCancel(cancelAmount);
            }
            
            paymentRepository.save(payment);

            log.info("결제 취소 완료: paymentKey={}, amount={}", paymentKey, cancelAmount);
            
        } catch (Exception e) {
            log.error("결제 취소 실패: paymentKey={}", paymentKey, e);
            throw new RuntimeException("결제 취소에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
