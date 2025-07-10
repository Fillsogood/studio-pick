package org.example.studiopick.application.studio.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.DashboardResponseDto;
import org.example.studiopick.infrastructure.statistics.DashboardStatsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardStatsMapper dashboardStatsMapper;

    public DashboardResponseDto getStudioDashboard(Long studioId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        // 통계 조회
        long todayReservationCount = dashboardStatsMapper.countTodayReservations(studioId, today);
        long monthReservationCount = dashboardStatsMapper.countMonthReservations(studioId, monthStart, monthEnd);
        long todayRevenue = dashboardStatsMapper.sumTodayRevenue(studioId, today);
        long monthRevenue = dashboardStatsMapper.sumMonthRevenue(studioId, monthStart, monthEnd);
        long newCustomerCount = dashboardStatsMapper.countNewCustomers(
                monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
        long classCount = dashboardStatsMapper.countClassesByStudioId(studioId);

        return DashboardResponseDto.builder()
                .todayReservationCount(todayReservationCount)
                .monthReservationCount(monthReservationCount)
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .newCustomerCount(newCustomerCount)
                .classCount(classCount)
                .build();
    }
}
