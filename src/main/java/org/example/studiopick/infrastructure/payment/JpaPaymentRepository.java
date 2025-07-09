package org.example.studiopick.infrastructure.payment;

import org.example.studiopick.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {

  /**
   * 결제 키로 결제 정보 조회
   */
  Optional<Payment> findByPaymentKey(String paymentKey);

  /**
   * 예약 ID로 결제 정보 조회
   */
  Optional<Payment> findByReservationId(Long reservationId);

  /**
   * 주문 ID로 결제 정보 조회
   */
  Optional<Payment> findByOrderId(String orderId);
}