package org.example.studiopick.application.admin;

import org.example.studiopick.application.admin.dto.reservation.*;

/**
 * 관리자 예약 서비스 인터페이스
 */
public interface AdminReservationService {
    
    /**
     * 전체 예약 목록 조회 (필터링, 페이징)
     */
    AdminReservationListResponse getAllReservations(
        int page, Integer size, String status, String startDate, String endDate,
        Long userId, Long studioId, String searchKeyword);
    
    /**
     * 예약 상세 조회
     */
    AdminReservationDetailResponse getReservationDetail(Long reservationId);
    
    /**
     * 예약 상태 변경 (관리자 권한)
     */
    AdminReservationStatusResponse changeReservationStatus(
        Long reservationId, AdminReservationStatusCommand command);
    
    /**
     * 예약 통계 조회
     */
    AdminReservationStatsResponse getReservationStats();
    
    /**
     * 사용자별 예약 내역 조회 (관리자용)
     */
    AdminReservationListResponse getUserReservations(
        Long userId, int page, int size, String status);
    
    /**
     * 스튜디오별 예약 내역 조회 (관리자용)
     */
    AdminReservationListResponse getStudioReservations(
        Long studioId, int page, int size, String status);
}
