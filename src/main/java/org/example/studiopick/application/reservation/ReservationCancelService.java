package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.CancelReservationCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCancelService {

    private final ReservationDomainService reservationDomainService;

    @Transactional
    public ReservationResponse cancelReservation(CancelReservationCommand command){
        //1. 예약 찾기
        Reservation reservation = reservationDomainService.findById(command.reservationId());

        //2. 유저 찾기
        if (!reservation.getUser().getId().equals(command.userId())){
            throw new IllegalStateException("사용자가 일치하지 않습니다.");
        }

        //3. 상태 변경
        reservation.cancel();

        //4. 응답 DTO로 감싸서 변환
        return new ReservationResponse(
                reservation.getId(),
                reservation.getTotalAmount(),
                reservation.getStatus()
        );

    }
}
