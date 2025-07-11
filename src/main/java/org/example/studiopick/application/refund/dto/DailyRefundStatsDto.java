package org.example.studiopick.application.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일별 환불 통계 DTO
 */
public record DailyRefundStatsDto(
    LocalDate refundDate,
    Long refundCount,
    BigDecimal totalAmount
) {
    
    /**
     * Object[] 배열에서 DTO로 변환
     */
    public static DailyRefundStatsDto from(Object[] result) {
        return new DailyRefundStatsDto(
            (LocalDate) result[0],
            (Long) result[1], 
            (BigDecimal) result[2]
        );
    }
}
