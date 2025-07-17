package org.example.studiopick.application.admin.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 대시보드 기간별 통계 응답
 */
public record AdminDashboardStatsResponse(
        String startDate,
        String endDate,
        List<DailyStats> dailyStats,
        PeriodComparison periodComparison
) {
    
    public record DailyStats(
            LocalDate date,
            long newUsers,
            long newReservations,
            BigDecimal dailySales,
            long newReports,
            long processedReports
    ) {}
    
    public record PeriodComparison(
            double userGrowthRate,
            double salesGrowthRate,
            double reservationGrowthRate,
            double reportResolutionRate,
            String trend
    ) {}
}
