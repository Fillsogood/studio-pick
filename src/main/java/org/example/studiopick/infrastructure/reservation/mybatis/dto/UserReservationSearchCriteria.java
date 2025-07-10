package org.example.studiopick.infrastructure.reservation.mybatis.dto;

import java.time.LocalDate;

/**
 * 사용자용 예약 검색 조건
 */
public record UserReservationSearchCriteria(
    Long userId,
    String status,
    LocalDate startDate,
    LocalDate endDate,
    Long studioId,
    int offset,
    int limit,
    String orderBy
) {
    
    public static UserReservationSearchCriteria of(
            Long userId, String status, LocalDate startDate, LocalDate endDate,
            Long studioId, int page, int size) {
        
        return new UserReservationSearchCriteria(
            userId, status, startDate, endDate, studioId,
            (page - 1) * size, size, "r.reservation_date DESC"
        );
    }
}
