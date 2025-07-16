package org.example.studiopick.application.reservation;

import org.example.studiopick.application.reservation.dto.*;

import java.time.LocalDate;

/**
 * 예약 서비스 인터페이스
 */
public interface ReservationService {

    /**
     * 스튜디오 예약 생성
     */
    ReservationResponse createStudioReservation(Long studioId, ReservationCreateCommand command, Long userId);

    /**
     * 공방 예약 생성
     */
    ReservationResponse createWorkshopReservation(Long workshopId, ReservationCreateCommand command, Long userId);

    /**
     * 예약 가능 시간 조회
     */
    AvailableTimesResponse getAvailableTimes(Long studioId, LocalDate date);

    /**
     * 사용자별 예약 목록 조회
     */
    UserReservationListResponse getUserReservations(
        Long userId, int page, int size, String status,
        LocalDate startDate, LocalDate endDate, Long studioId);

    /**
     * 예약 상세 조회
     */
    UserReservationDetailResponse getReservationDetail(Long reservationId, Long userId);

    /**
     * 예약 취소
     */
    ReservationCancelResponse cancelReservation(Long id, ReservationCancelRequest request);

    /**
     * 예약 결제 확정
     */
    void confirmReservationPayment(Long reservationId);
}
