package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationRefundApproveService {

    private final ReservationDomainService reservationDomainService;

    @Transactional
    public ReservationResponse approveRefund(Long reservationId) {
        Reservation reservation = reservationDomainService.findById(reservationId);

        if (!reservation.isCancelled()) {
            throw new IllegalStateException("환불은 'CANCELLED' 상태인 예약만 가능합니다.");
        }

        reservation.refund(); // 상태를 REFUNDED로 변경

        return new ReservationResponse(
                reservation.getId(),
                reservation.getTotalAmount(),
                reservation.getStatus(),
                reservation.getTotalAmount() // 실제 환불 처리 금액
        );
    }
}
