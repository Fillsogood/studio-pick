package org.example.studiopick.infrastructure.reservation.mybatis.dto;

import java.time.LocalDate;

/**
 * 관리자용 예약 검색 조건
 */
public record ReservationSearchCriteria(
    String status,
    LocalDate startDate,
    LocalDate endDate,
    Long userId,
    Long studioId,
    String searchKeyword,
    int offset,
    int limit,
    String orderBy
) {
    
    public static ReservationSearchCriteria of(
            String status, String startDate, String endDate,
            Long userId, Long studioId, String searchKeyword,
            int page, int size) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
        
        return new ReservationSearchCriteria(
            status, start, end, userId, studioId, searchKeyword,
            (page - 1) * size, size, "r.reservation_date DESC, r.start_time DESC"
        );
    }
}
