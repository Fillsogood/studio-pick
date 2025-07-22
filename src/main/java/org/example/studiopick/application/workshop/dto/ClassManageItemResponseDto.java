package org.example.studiopick.application.workshop.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 호스트 클래스 관리 페이지용 리스트 아이템 DTO
 */
public record ClassManageItemResponseDto(
        Long id,
        String title,
        LocalDate date,
        String status,
        int reservationCount,
        BigDecimal totalRevenue
) {
}
