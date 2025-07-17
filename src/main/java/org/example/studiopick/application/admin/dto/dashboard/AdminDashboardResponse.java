package org.example.studiopick.application.admin.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관리자 대시보드 메인 응답
 */
public record AdminDashboardResponse(
        // 사용자 통계
        UserStats userStats,
        
        // 스튜디오 통계
        StudioStats studioStats,
        
        // 예약 통계
        ReservationStats reservationStats,
        
        // 매출 통계
        SalesStats salesStats,
        
        // 신고 통계
        ReportStats reportStats,
        
        // 업데이트 시간
        LocalDateTime lastUpdated
) {
    
    public record UserStats(
            long totalUsers,
            long activeUsers,
            long newUsersToday,
            long studioOwners,
            double growthRate
    ) {}
    
    public record StudioStats(
            long totalStudios,
            long activeStudios,
            long pendingApprovals,
            long newApplicationsToday,
            double approvalRate
    ) {}
    
    public record ReservationStats(
            long totalReservations,
            long todayReservations,
            long pendingReservations,
            long completedReservations,
            double completionRate
    ) {}
    
    public record SalesStats(
            BigDecimal totalSales,
            BigDecimal todaySales,
            BigDecimal monthSales,
            BigDecimal averageOrderValue,
            double growthRate
    ) {}
    
    public record ReportStats(
            long totalReports,
            long pendingReports,
            long processedToday,
            long autoHiddenContent,
            double resolutionRate
    ) {}
}
