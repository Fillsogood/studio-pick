package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.dashboard.*;
import org.example.studiopick.domain.common.enums.ReportStatus;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.StudioStatus;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.payment.JpaPaymentRepository;
import org.example.studiopick.infrastructure.refund.RefundRepository;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자 대시보드 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final JpaUserRepository userRepository;
    private final JpaStudioRepository studioRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaPaymentRepository paymentRepository;
    private final ReportRepository reportRepository;
    private final RefundRepository refundRepository;

    @Override
    public AdminDashboardResponse getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 사용자 통계
        AdminDashboardResponse.UserStats userStats = getUserStats(today);
        
        // 스튜디오 통계
        AdminDashboardResponse.StudioStats studioStats = getStudioStats(today);
        
        // 예약 통계
        AdminDashboardResponse.ReservationStats reservationStats = getReservationStats(today);
        
        // 매출 통계
        AdminDashboardResponse.SalesStats salesStats = getSalesStats(todayStart, todayEnd);
        
        // 신고 통계
        AdminDashboardResponse.ReportStats reportStats = getReportStats(today);

        return new AdminDashboardResponse(
            userStats,
            studioStats,
            reservationStats,
            salesStats,
            reportStats,
            now
        );
    }

    @Override
    public AdminDashboardStatsResponse getDashboardStats(LocalDate startDate, LocalDate endDate) {
        List<AdminDashboardStatsResponse.DailyStats> dailyStats = getDailyStats(startDate, endDate);
        AdminDashboardStatsResponse.PeriodComparison periodComparison = getPeriodComparison(startDate, endDate);

        return new AdminDashboardStatsResponse(
            startDate.toString(),
            endDate.toString(),
            dailyStats,
            periodComparison
        );
    }

    @Override
    public AdminRealTimeStatsResponse getRealTimeStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 실시간 데이터 조회
        long currentActiveUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long todaySignups = userRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long todayReservations = reservationRepository.countByCreatedAtBetween(todayStart, todayEnd);
        BigDecimal todaySales = paymentRepository.getSalesByDateRangeAndStatus(
            todayStart, todayEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);

        AdminRealTimeStatsResponse.PendingCounts pendingCounts = new AdminRealTimeStatsResponse.PendingCounts(
            reservationRepository.countByStatus(ReservationStatus.PENDING),
            reportRepository.countByStatus(ReportStatus.PENDING),
            studioRepository.countByStatus(StudioStatus.PENDING),
            refundRepository.countByStatus(org.example.studiopick.domain.common.enums.RefundStatus.PENDING)
        );

        return new AdminRealTimeStatsResponse(
            currentActiveUsers,
            todaySignups,
            todayReservations,
            todaySales != null ? todaySales : BigDecimal.ZERO,
            pendingCounts,
            LocalDateTime.now()
        );
    }

    @Override
    public AdminKpiSummaryResponse getKpiSummary() {
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.atTime(LocalTime.MAX);

        // 운영 KPI
        AdminKpiSummaryResponse.OperationalKpi operationalKpi = getOperationalKpi();

        // 품질 KPI
        AdminKpiSummaryResponse.QualityKpi qualityKpi = getQualityKpi();

        // 성장 KPI
        AdminKpiSummaryResponse.GrowthKpi growthKpi = getGrowthKpi();

        return new AdminKpiSummaryResponse(
            operationalKpi,
            qualityKpi,
            growthKpi
        );
    }

    // Private helper methods
    
    private AdminDashboardResponse.UserStats getUserStats(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long newUsersToday = userRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long studioOwners = userRepository.countByRole(org.example.studiopick.domain.common.enums.UserRole.STUDIO_OWNER);
        
        // 성장률 계산 (전월 대비)
        LocalDate lastMonth = today.minusMonths(1);
        LocalDateTime lastMonthStart = lastMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(LocalTime.MAX);
        long lastMonthUsers = userRepository.countByCreatedAtBetween(lastMonthStart, lastMonthEnd);
        double growthRate = lastMonthUsers > 0 ? ((double) newUsersToday / lastMonthUsers) * 100 : 0.0;

        return new AdminDashboardResponse.UserStats(
            totalUsers, activeUsers, newUsersToday, studioOwners, growthRate
        );
    }

    private AdminDashboardResponse.StudioStats getStudioStats(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        long totalStudios = studioRepository.count();
        long activeStudios = studioRepository.countByStatus(StudioStatus.ACTIVE);
        long pendingApprovals = studioRepository.countByStatus(StudioStatus.PENDING);
        long newApplicationsToday = studioRepository.countByCreatedAtBetween(todayStart, todayEnd);
        
        double approvalRate = totalStudios > 0 ? ((double) activeStudios / totalStudios) * 100 : 0.0;

        return new AdminDashboardResponse.StudioStats(
            totalStudios, activeStudios, pendingApprovals, newApplicationsToday, approvalRate
        );
    }

    private AdminDashboardResponse.ReservationStats getReservationStats(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        long totalReservations = reservationRepository.count();
        long todayReservations = reservationRepository.countByCreatedAtBetween(todayStart, todayEnd);
        long pendingReservations = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long completedReservations = reservationRepository.countByStatus(ReservationStatus.COMPLETED);
        
        double completionRate = totalReservations > 0 ? ((double) completedReservations / totalReservations) * 100 : 0.0;

        return new AdminDashboardResponse.ReservationStats(
            totalReservations, todayReservations, pendingReservations, completedReservations, completionRate
        );
    }

    private AdminDashboardResponse.SalesStats getSalesStats(LocalDateTime todayStart, LocalDateTime todayEnd) {
        LocalDateTime monthStart = todayStart.toLocalDate().withDayOfMonth(1).atStartOfDay();
        
        BigDecimal totalSales = paymentRepository.getTotalSalesByStatus(
            org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        BigDecimal todaySales = paymentRepository.getSalesByDateRangeAndStatus(
            todayStart, todayEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        BigDecimal monthSales = paymentRepository.getSalesByDateRangeAndStatus(
            monthStart, todayEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        
        // 평균 주문 금액 계산
        long totalOrders = paymentRepository.countByStatus(org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        BigDecimal averageOrderValue = totalOrders > 0 && totalSales != null ? 
            totalSales.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        // 성장률 계산 (전월 대비)
        LocalDate currentMonth = todayStart.toLocalDate();
        LocalDate lastMonth = currentMonth.minusMonths(1);
        LocalDateTime lastMonthStart = lastMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(LocalTime.MAX);
        
        BigDecimal lastMonthSales = paymentRepository.getSalesByDateRangeAndStatus(
            lastMonthStart, lastMonthEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        
        double growthRate = 0.0;
        if (lastMonthSales != null && lastMonthSales.compareTo(BigDecimal.ZERO) > 0 && monthSales != null) {
            growthRate = monthSales.subtract(lastMonthSales)
                .divide(lastMonthSales, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }

        return new AdminDashboardResponse.SalesStats(
            totalSales != null ? totalSales : BigDecimal.ZERO,
            todaySales != null ? todaySales : BigDecimal.ZERO,
            monthSales != null ? monthSales : BigDecimal.ZERO,
            averageOrderValue,
            growthRate
        );
    }

    private AdminDashboardResponse.ReportStats getReportStats(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long processedToday = reportRepository.countByProcessedAtBetween(todayStart, todayEnd);
        long autoHiddenContent = reportRepository.countByStatus(ReportStatus.AUTO_HIDDEN);
        
        long processedReports = reportRepository.countByStatusNot(ReportStatus.PENDING);
        double resolutionRate = totalReports > 0 ? ((double) processedReports / totalReports) * 100 : 0.0;

        return new AdminDashboardResponse.ReportStats(
            totalReports, pendingReports, processedToday, autoHiddenContent, resolutionRate
        );
    }

    private List<AdminDashboardStatsResponse.DailyStats> getDailyStats(LocalDate startDate, LocalDate endDate) {
        List<AdminDashboardStatsResponse.DailyStats> stats = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(LocalTime.MAX);
            
            long newUsers = userRepository.countByCreatedAtBetween(dayStart, dayEnd);
            long newReservations = reservationRepository.countByCreatedAtBetween(dayStart, dayEnd);
            BigDecimal dailySales = paymentRepository.getSalesByDateRangeAndStatus(
                dayStart, dayEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
            long newReports = reportRepository.countByCreatedAtBetween(dayStart, dayEnd);
            long processedReports = reportRepository.countByProcessedAtBetween(dayStart, dayEnd);
            
            stats.add(new AdminDashboardStatsResponse.DailyStats(
                current, newUsers, newReservations, 
                dailySales != null ? dailySales : BigDecimal.ZERO,
                newReports, processedReports
            ));
            
            current = current.plusDays(1);
        }
        
        return stats;
    }

    private AdminDashboardStatsResponse.PeriodComparison getPeriodComparison(LocalDate startDate, LocalDate endDate) {
        // 이전 기간과의 비교 데이터 계산
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(days + 1);
        LocalDate prevEndDate = startDate.minusDays(1);
        
        LocalDateTime currentStart = startDate.atStartOfDay();
        LocalDateTime currentEnd = endDate.atTime(LocalTime.MAX);
        LocalDateTime prevStart = prevStartDate.atStartOfDay();
        LocalDateTime prevEnd = prevEndDate.atTime(LocalTime.MAX);
        
        // 현재 기간 데이터
        long currentUsers = userRepository.countByCreatedAtBetween(currentStart, currentEnd);
        long currentReservations = reservationRepository.countByCreatedAtBetween(currentStart, currentEnd);
        BigDecimal currentSales = paymentRepository.getSalesByDateRangeAndStatus(
            currentStart, currentEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        
        // 이전 기간 데이터
        long prevUsers = userRepository.countByCreatedAtBetween(prevStart, prevEnd);
        long prevReservations = reservationRepository.countByCreatedAtBetween(prevStart, prevEnd);
        BigDecimal prevSales = paymentRepository.getSalesByDateRangeAndStatus(
            prevStart, prevEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        
        // 증감률 계산
        double userGrowth = prevUsers > 0 ? ((double) (currentUsers - prevUsers) / prevUsers) * 100 : 0.0;
        double reservationGrowth = prevReservations > 0 ? ((double) (currentReservations - prevReservations) / prevReservations) * 100 : 0.0;
        
        double salesGrowth = 0.0;
        if (prevSales != null && prevSales.compareTo(BigDecimal.ZERO) > 0 && currentSales != null) {
            salesGrowth = currentSales.subtract(prevSales).divide(prevSales, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
        }
        
        double avgGrowth = (userGrowth + reservationGrowth + salesGrowth) / 3.0;
        String trend = avgGrowth > 5 ? "GROWTH" : avgGrowth < -5 ? "DECLINE" : "STABLE";
        
        return new AdminDashboardStatsResponse.PeriodComparison(
            userGrowth, reservationGrowth, salesGrowth, avgGrowth, trend
        );
    }



    private AdminKpiSummaryResponse.OperationalKpi getOperationalKpi() {
        // 실제 데이터 기반 운영 KPI 계산
        long totalUsers = userRepository.count();
        long totalReservations = reservationRepository.count();
        
        // 예약 성공률 계산
        double reservationSuccessRate = totalReservations > 0 ? 
            (double) reservationRepository.countByStatus(ReservationStatus.COMPLETED) / totalReservations * 100 : 0.0;
        
        // 시스템 업타임 (예시: 99.9% - 실제 모니터링 시스템에서 가져와야 함)
        double systemUptime = 99.9;
        
        // 사용자 만족도 (예시: 리뷰 평균 점수 기반)
        double userSatisfaction = 4.5 * 20; // 5점 만점을 100점으로 변환

        return new AdminKpiSummaryResponse.OperationalKpi(
            systemUptime, totalUsers, userSatisfaction, reservationSuccessRate
        );
    }

    private AdminKpiSummaryResponse.QualityKpi getQualityKpi() {
        // 실제 데이터 기반 품질 KPI 계산


        
        // 신고 처리율 계산
        long totalReports = reportRepository.count();
        long processedReports = reportRepository.countByStatusNot(ReportStatus.PENDING);
        double resolutionRate = totalReports > 0 ? (double) processedReports / totalReports * 100 : 0.0;
        
        // 현재 대기 중인 미처리 신고 수
        long complaints = reportRepository.countByStatus(ReportStatus.PENDING);
        
        // 환불 성공률 계산
        long totalRefunds = refundRepository.count();
        long completedRefunds = refundRepository.countByStatus(org.example.studiopick.domain.common.enums.RefundStatus.COMPLETED);
        double refundSuccessRate = totalRefunds > 0 ? (double) completedRefunds / totalRefunds * 100 : 100.0;

        return new AdminKpiSummaryResponse.QualityKpi(
            resolutionRate, complaints, refundSuccessRate
        );
    }

    private AdminKpiSummaryResponse.GrowthKpi getGrowthKpi() {
        // 실제 데이터 기반 성장 KPI 계산
        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);
        
        // 이번달과 전월 기간 설정
        LocalDateTime thisMonthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime thisMonthEnd = now.atTime(LocalTime.MAX);
        LocalDateTime lastMonthStart = lastMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).atTime(LocalTime.MAX);
        
        // 사용자 성장률 계산
        long thisMonthUsers = userRepository.countByCreatedAtBetween(thisMonthStart, thisMonthEnd);
        long lastMonthUsers = userRepository.countByCreatedAtBetween(lastMonthStart, lastMonthEnd);
        double userGrowthRate = lastMonthUsers > 0 ? ((double) (thisMonthUsers - lastMonthUsers) / lastMonthUsers) * 100 : 0.0;
        
        // 매출 성장률 계산
        BigDecimal thisMonthSales = paymentRepository.getSalesByDateRangeAndStatus(
            thisMonthStart, thisMonthEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        BigDecimal lastMonthSales = paymentRepository.getSalesByDateRangeAndStatus(
            lastMonthStart, lastMonthEnd, org.example.studiopick.domain.common.enums.PaymentStatus.PAID);
        
        double revenueGrowthRate = 0.0;
        if (lastMonthSales != null && lastMonthSales.compareTo(BigDecimal.ZERO) > 0 && thisMonthSales != null) {
            revenueGrowthRate = thisMonthSales.subtract(lastMonthSales)
                .divide(lastMonthSales, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        
        // 예약 성장률 계산
        long thisMonthReservations = reservationRepository.countByCreatedAtBetween(thisMonthStart, thisMonthEnd);
        long lastMonthReservations = reservationRepository.countByCreatedAtBetween(lastMonthStart, lastMonthEnd);
        double reservationGrowthRate = lastMonthReservations > 0 ? 
            ((double) (thisMonthReservations - lastMonthReservations) / lastMonthReservations) * 100 : 0.0;
        
        // 신규 스튜디오 승인률
        long totalStudioApplications = studioRepository.count();
        long approvedStudios = studioRepository.countByStatus(StudioStatus.ACTIVE);
        double newStudioAcquisitionRate = totalStudioApplications > 0 ? 
            (double) approvedStudios / totalStudioApplications * 100 : 0.0;
        
        // 사용자 유지율 (예시: 지난 3개월 내 활동한 사용자 비율)
        LocalDateTime threeMonthsAgo = now.minusMonths(3).atStartOfDay();
        long activeUsersLast3Months = userRepository.countByCreatedAtBetween(threeMonthsAgo, thisMonthEnd);
        long totalActiveUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        double retentionRate = totalActiveUsers > 0 ? 
            (double) activeUsersLast3Months / totalActiveUsers * 100 : 0.0;

        return new AdminKpiSummaryResponse.GrowthKpi(
            userGrowthRate, revenueGrowthRate, reservationGrowthRate, newStudioAcquisitionRate, retentionRate
        );
    }
}
