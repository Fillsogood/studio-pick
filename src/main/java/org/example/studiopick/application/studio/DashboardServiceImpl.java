package org.example.studiopick.application.studio;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.DashboardResponseDto;
import org.example.studiopick.infrastructure.studio.mybatis.StudioDashboardStatsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private final StudioDashboardStatsMapper studioDashboardStatsMapper;

  public DashboardResponseDto getStudioDashboard(Long studioId) {
    LocalDate today = LocalDate.now();
    YearMonth currentMonth = YearMonth.now();
    LocalDate monthStart = currentMonth.atDay(1);
    LocalDate monthEnd = currentMonth.atEndOfMonth();

    // 통계 조회
    long todayReservationCount = studioDashboardStatsMapper.countTodayReservations(studioId, today);
    long monthReservationCount = studioDashboardStatsMapper.countMonthReservations(studioId, monthStart, monthEnd);
    long todayRevenue = studioDashboardStatsMapper.sumTodayRevenue(studioId, today);
    long monthRevenue = studioDashboardStatsMapper.sumMonthRevenue(studioId, monthStart, monthEnd);
    long newCustomerCount = studioDashboardStatsMapper.countNewCustomers(
        monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
    long classCount = studioDashboardStatsMapper.countClassesByStudioId(studioId);

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
