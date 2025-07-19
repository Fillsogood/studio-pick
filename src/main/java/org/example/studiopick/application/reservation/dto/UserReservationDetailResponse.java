package org.example.studiopick.application.reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record UserReservationDetailResponse(
    Long id,                    // 예약 ID
    String type,                // "studio" or "workshop"
    StudioInfo studio,               // 스튜디오 예약인 경우만 채움
    WorkshopInfo workshop,           // 클래스 예약인 경우만 채움
    LocalDate reservationDate,  // 예약 날짜
    LocalTime startTime,        // 시작 시간
    LocalTime endTime,          // 종료 시간
    Short peopleCount,          // 인원 수
    Long totalAmount,           // 총 금액
    String status,              // 예약 상태
    String cancelReason,        // 취소 사유 (취소된 경우)
    LocalDateTime cancelledAt,  // 취소 시간 (취소된 경우)
    LocalDateTime createdAt,    // 예약 생성 시간
    LocalDateTime updatedAt     // 예약 수정 시간
) {
    
    public record StudioInfo(
        Long id,                // 스튜디오 ID
        String name,            // 스튜디오 이름
        String phone,           // 스튜디오 전화번호
        String location,        // 스튜디오 위치
        Long hourlyBaseRate,    // 시간당 기본 요금
        Long perPersonRate      // 인당 추가 요금
    ) {}

    public record WorkshopInfo(
            Long id,
            String title,
            String instructor,
            String address,
            String thumbnailUrl, // 썸네일 포함
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal price
    ) {}
}
