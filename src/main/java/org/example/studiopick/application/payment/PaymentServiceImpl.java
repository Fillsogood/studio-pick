package org.example.studiopick.application.payment;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.payment.dto.*;
import org.example.studiopick.application.reservation.ReservationService;
import org.example.studiopick.domain.common.enums.PaymentMethod;
import org.example.studiopick.domain.common.enums.PaymentStatus;
import org.example.studiopick.domain.payment.Payment;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.example.studiopick.application.reservation.dto.PaginationResponse;
import org.example.studiopick.infrastructure.User.JpaUserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private  final TossPaymentsService tossPaymentsService;
    private final JpaPaymentRepository paymentRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaUserRepository userRepository;
    private final ReservationService reservationService;

    @Value("${toss.payments.test-client-key}")
    private String clientKey;

    /**
     * 결제 요청 (결제 정보 생성) - 기존과 동일
     */
    @Override
    @Transactional
    public PaymentRequestResponse requestPayment(PaymentRequestCommand command) {

        // 1. 예약 정보 조회
        Reservation reservation = reservationRepository.findById(command.reservationId())
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // ✅ 금액 비교 수정 (BigDecimal 안전 비교)
        BigDecimal requestAmount = command.amount();
        BigDecimal reservationAmount = BigDecimal.valueOf(reservation.getTotalAmount());

        // 요청 금액과 예약 금액 일치 확인
        if (!command.amount().equals(BigDecimal.valueOf(reservation.getTotalAmount()))) {
            throw new IllegalArgumentException("결제 금액이 예약 금액과 일치하지 않습니다.");
        }

        // 2. 주문 ID 생성
        String orderId = generateOrderId();

        // 3. 결제 정보 생성
        Payment payment = Payment.builder()
            .reservation(reservation)
            .amount(command.amount())
            .status(PaymentStatus.READY)
            .build();

        // 4. orderId와 기본 정보 설정
        payment.updateOrderId(orderId);

        paymentRepository.save(payment);

        log.info("결제 요청 생성 완료: orderId={}, amount={}", orderId, command.amount());

        return new PaymentRequestResponse(
            orderId,
            command.amount(),
            command.orderName(),
            command.customerName(),
            clientKey
        );
    }

    /**
     * ✅ 수정된 결제 승인 메서드
     */
    @Override
    @Transactional
    public PaymentConfirmResponse confirmPayment(PaymentConfirmCommand command) {

        log.info("결제 승인 처리 시작: paymentKey={}, orderId={}, amount={}",
            command.paymentKey(), command.orderId(), command.amount());

        // ✅ 1. 입력값 검증
        if (command.paymentKey() == null || command.paymentKey().trim().isEmpty()) {
            throw new IllegalArgumentException("PaymentKey는 필수입니다.");
        }
        if (command.orderId() == null || command.orderId().trim().isEmpty()) {
            throw new IllegalArgumentException("OrderId는 필수입니다.");
        }

        // ✅ 2. 우리 DB에서 결제 정보 먼저 조회
        Payment payment = paymentRepository.findByOrderId(command.orderId())
            .orElseThrow(() -> {
                log.error("결제 정보를 찾을 수 없습니다: orderId={}", command.orderId());
                return new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + command.orderId());
            });

        // ✅ 3. 결제 상태 확인
        if (payment.getStatus() == PaymentStatus.DONE) {
            log.warn("이미 완료된 결제입니다: orderId={}", command.orderId());
            throw new IllegalStateException("이미 완료된 결제입니다.");
        }

        // ✅ 4. 금액 검증
        BigDecimal requestAmount = command.amount();
        BigDecimal dbAmount = payment.getAmount();

        if (requestAmount.compareTo(dbAmount) != 0) {
            log.error("결제 금액 불일치: 요청금액={}, DB저장금액={}", requestAmount, dbAmount);
            throw new IllegalArgumentException(
                String.format("결제 금액이 일치하지 않습니다. 요청: %s, DB: %s", requestAmount, dbAmount));
        }

        try {
            // ✅ 6. 토스페이먼츠 결제 승인 호출
            TossPaymentResponse tossResponse = tossPaymentsService.confirmPayment(
                command.paymentKey(), command.orderId(), command.amount().longValue());

            // ✅ 7. 응답 검증
            if (tossResponse == null) {
                throw new RuntimeException("토스페이먼츠 응답이 null입니다.");
            }

            // ✅ 8. 결제 정보 업데이트
            payment.updatePaymentKey(command.paymentKey());
            payment.changeStatus(PaymentStatus.DONE);

            // 결제 방법 안전하게 설정
            if (tossResponse.method() != null) {
                try {
                    payment.updateMethod(PaymentMethod.valueOf(tossResponse.method().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 결제 방법: {}", tossResponse.method());
                    // 기본값 설정 또는 그대로 null 유지
                }
            }

            payment.markAsPaid();

            // 거래 키 설정
            if (tossResponse.transactionKey() != null) {
                payment.updateTransactionKey(tossResponse.transactionKey());
            }

            // 실패 정보 설정
            if (tossResponse.failure() != null) {
                payment.updateFailureInfo(
                    tossResponse.failure().code(),
                    tossResponse.failure().message()
                );
                log.warn("결제 승인되었지만 실패 정보 포함: code={}, message={}",
                    tossResponse.failure().code(), tossResponse.failure().message());
            }

            paymentRepository.saveAndFlush(payment);
            log.info("결제 정보 업데이트 완료: paymentKey={}", command.paymentKey());

            // ✅ 9. 예약 상태 업데이트 (CONFIRMED로)
            reservationService.confirmReservationPayment(payment.getReservation().getId());
            log.info("예약 상태 업데이트 완료: reservationId={}", payment.getReservation().getId());

            return new PaymentConfirmResponse(
                command.paymentKey(),
                command.orderId(),
                tossResponse.status(),
                command.amount(),
                tossResponse.method(),
                tossResponse.approvedAt()
            );

        } catch (Exception e) {
            // ✅ 10. 실패 시 결제 상태를 FAILED로 업데이트
            try {
                payment.updateFailureInfo("CONFIRM_FAILED", e.getMessage());
                paymentRepository.save(payment);
                log.error("결제 승인 실패로 상태 업데이트: orderId={}", command.orderId());
            } catch (Exception updateEx) {
                log.error("결제 실패 상태 업데이트 중 오류: {}", updateEx.getMessage());
            }

            log.error("결제 승인 처리 실패: paymentKey={}, orderId={}, error={}",
                command.paymentKey(), command.orderId(), e.getMessage(), e);
            throw e; // 원본 예외 재발생
        }
    }

    /**
     * ✅ 새로 추가: 결제 전 검증 메서드
     */
    @Override
    public void validatePaymentBeforeConfirm(String paymentKey, String orderId) {

        // 1. DB에서 결제 정보 존재 여부 확인
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다: " + orderId));

        // 2. 결제 상태 확인
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다: " + payment.getStatus());
        }

        // 3. 토스페이먼츠에서 결제 정보 확인
        try {
            TossPaymentResponse tossPayment = tossPaymentsService.getPayment(paymentKey);
            log.info("토스페이먼츠 결제 정보 확인 완료: status={}", tossPayment.status());
        } catch (Exception e) {
            log.warn("토스페이먼츠 결제 정보 확인 실패: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 PaymentKey입니다: " + paymentKey);
        }
    }

    /**
     * 결제 취소
     */
    @Override
    @Transactional
    public PaymentCancelResponse cancelPayment(String paymentKey, PaymentCancelCommand command) {

        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 2. 취소 가능 상태 확인
        if (!payment.isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 결제 상태입니다: " + payment.getStatus());
        }

        // 3. 토스페이먼츠 결제 취소 호출
        TossPaymentCancelResponse tossResponse = tossPaymentsService.cancelPayment(
            paymentKey, command.cancelReason());

        // 4. 결제 상태 업데이트
        payment.cancel();
        paymentRepository.save(payment);

        log.info("결제 취소 완료: paymentKey={}, amount={}, reason={}", 
            paymentKey, command.cancelAmount(), command.cancelReason());

        return new PaymentCancelResponse(
            paymentKey,
            "CANCELLED",
            command.cancelAmount(),
            command.cancelReason(),
            LocalDateTime.now()
        );
    }

    /**
     * 기타 메서드들은 기존과 동일
     */
    @Override
    public PaymentInfoResponse getPaymentInfo(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        return toPaymentInfoResponse(payment);
    }

    @Override
    public PaymentInfoResponse getPaymentByReservation(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        return toPaymentInfoResponse(payment);
    }

    private PaymentInfoResponse toPaymentInfoResponse(Payment payment) {
        return new PaymentInfoResponse(
            payment.getId(),
            payment.getPaymentKey(),
            payment.getOrderId(),
            payment.getReservation().getId(),
            payment.getAmount(),
            payment.getMethod(),
            payment.getStatus(),
            payment.getPaidAt(),
            payment.getFailureCode(),
            payment.getFailureReason()
        );
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @Override
    public UserPaymentHistoryListResponse getUserPaymentHistory(Long userId, int page, int size,
                                                                String status, LocalDate startDate, LocalDate endDate) {
        // 1. 입력값 검증
        if (page < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1 이상 100 이하여야 합니다.");
        }
        
        // 2. 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Payment> paymentsPage = getFilteredUserPayments(userId, status, startDate, endDate, pageable);
        
        List<UserPaymentHistoryResponse> payments = paymentsPage.getContent()
                .stream()
                .map(this::toUserPaymentHistoryResponse)
                .toList();
                
        return new UserPaymentHistoryListResponse(
                payments,
                new PaginationResponse(page, paymentsPage.getTotalElements())
        );
    }
    
    /**
     * 필터 조건에 따른 사용자 결제 내역 조회
     */
    private Page<Payment> getFilteredUserPayments(Long userId, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        PaymentStatus paymentStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("잘못된 결제 상태입니다: " + status);
            }
        }
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        // 상태 + 기간
        if (paymentStatus != null && startDateTime != null && endDateTime != null) {
            return paymentRepository.findByUserIdAndPaidAtBetweenAndStatusOrderByPaidAtDesc(
                    userId, startDateTime, endDateTime, paymentStatus, pageable);
        }
        // 상태만
        else if (paymentStatus != null) {
            return paymentRepository.findByUserIdAndStatusOrderByPaidAtDesc(userId, paymentStatus, pageable);
        }
        // 기간만
        else if (startDateTime != null && endDateTime != null) {
            return paymentRepository.findByUserIdAndPaidAtBetweenOrderByPaidAtDesc(
                    userId, startDateTime, endDateTime, pageable);
        }
        // 필터 없음
        else {
            return paymentRepository.findByUserIdOrderByPaidAtDesc(userId, pageable);
        }
    }
    
    /**
     * Payment 엔티티를 UserPaymentHistoryResponse DTO로 변환
     */
    private UserPaymentHistoryResponse toUserPaymentHistoryResponse(Payment payment) {
        Reservation reservation = payment.getReservation();
        return new UserPaymentHistoryResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                reservation.getId(),
                reservation.getStudio().getName(),
                reservation.getReservationDate().toString(),
                reservation.getStartTime() + " - " + reservation.getEndTime(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getFailureReason()
        );
    }

    /**
     * ✅ 개선된 주문 ID 생성 (더 안전하게)
     */
    private String generateOrderId() {
        // 형식: PREFIX_TIMESTAMP_RANDOM
        String prefix = "STUDIO";
        long timestamp = System.currentTimeMillis();
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        String orderId = String.format("%s_%d_%s", prefix, timestamp, randomPart);

        // 중복 확인 (만약의 경우)
        while (paymentRepository.findByOrderId(orderId).isPresent()) {
            randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            orderId = String.format("%s_%d_%s", prefix, timestamp, randomPart);
        }

        return orderId;
    }
}
