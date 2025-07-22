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
import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.refund.Refund;
import org.example.studiopick.infrastructure.payment.JpaSettlementRepository;
import org.example.studiopick.infrastructure.refund.RefundRepository;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
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
    private final JpaReservationRepository reservationRepository;
    private final JpaSettlementRepository settlementRepository;

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

        log.info("환불 직전 Payment 상태: {}", payment.getStatus());
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
            .user(reservation.getUser())
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

            settlementRepository.findByPaymentId(payment.getId())
                .ifPresent(settlement -> {
                    settlement.changeStatus(SettlementStatus.CANCELLED);
                    settlementRepository.save(settlement);
                    log.info("정산 상태 변경: settlementId={}, paymentId={}", settlement.getId(), payment.getId());
                });

            // 8. 결제 상태 업데이트
            if (refundInfo.refundAmount().compareTo(payment.getAmount()) >= 0) {
                payment.cancel(); // 전액 취소
            } else {
                payment.partialCancel(refundInfo.refundAmount()); // 부분 취소
            }
            paymentRepository.save(payment);

            // ✅ 9. 예약 상태 업데이트 (추가된 부분)
            reservation.refund();
            reservationRepository.save(reservation);

            log.info("환불 및 예약 상태 처리 최종 완료: reservationId={}, refundId={}, status={}",
                reservation.getId(), savedRefund.getId(), reservation.getStatus());

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
}
