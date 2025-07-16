package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.reservation.*;

/**
 * 관리자 예약 관리 서비스 인터페이스
 */
public interface AdminReservationService {

    /**
     * 모든 예약 조회 (페이징, 필터링 지원)
     */
    AdminReservationListResponse getAllReservations(
            int page, Integer size, String status, String startDate, String endDate,
            Long userId, Long studioId, String searchKeyword);

    /**
     * 예약 상세 조회
     */
    AdminReservationDetailResponse getReservationDetail(Long reservationId);

    /**
     * 예약 상태 변경
     */
    AdminReservationStatusResponse changeReservationStatus(
            Long reservationId, AdminReservationStatusCommand command);

    /**
     * 예약 통계 조회
     */
    AdminReservationStatsResponse getReservationStats();

    /**
     * 특정 사용자의 예약 목록 조회
     */
    AdminReservationListResponse getUserReservations(
            Long userId, int page, int size, String status);

    /**
     * 특정 스튜디오의 예약 목록 조회
     */
    AdminReservationListResponse getStudioReservations(
            Long studioId, int page, int size, String status);
}
