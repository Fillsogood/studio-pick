package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 상태 관리 서비스
 * PaymentService와 ReservationService 간의 순환 의존성 해결을 위해 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationStatusService {

    private final JpaReservationRepository reservationRepository;

    /**
     * 결제 완료 시 예약 확정
     */
    @Transactional
    public void confirmReservationPayment(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("예약 상태가 PENDING이 아닙니다.");
        }

        reservation.confirm();
        reservationRepository.save(reservation);
        
        log.info("예약 결제 확정 완료: reservationId={}", reservationId);
    }
}
