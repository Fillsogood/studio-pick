package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.reservation.dto.CancelReservationCommand;
import org.example.studiopick.application.reservation.dto.ReservationResponse;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

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

        //3. 예약 시작 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStart = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());

        // 4. 이미 시작된 예약은 취소 불가
        if (now.isAfter(reservationStart)){
            throw new IllegalStateException("이미 시작된 예약은 취소 할 수 없습니다.");
        }

        // 5. 환불 정책 계산
        Long refundAmount = calculateRefundAmount(now, reservationStart, reservation.getTotalAmount());

        // 6. 상태 변경
        reservation.cancel();

        // 7. 응답 DTO로 감싸서 반환
        return new ReservationResponse(
                reservation.getId(),
                reservation.getTotalAmount(),
                reservation.getStatus(),
                refundAmount
        );
    }

        // 환불 금액 계산 로직(24시간 전: 전액, 12시간 전 : 50%, 12시간 이내: 0원)
        private Long calculateRefundAmount(LocalDateTime now, LocalDateTime startTime, Long totalAmount) {
            long hoursUntilStart = Duration.between(now, startTime).toHours();

            if (hoursUntilStart >= 24) {
                return totalAmount; // 전액 환불
            } else if (hoursUntilStart >= 12) {
                return totalAmount / 2; // 50% 환불
            } else {
                return 0L; // 환불 불가
            }

    }
    // 으아아아아악 이거 하다가 메인에 머지해서 다시 디벨롭 머지용으로 수정함 ㅈㅈ
}
