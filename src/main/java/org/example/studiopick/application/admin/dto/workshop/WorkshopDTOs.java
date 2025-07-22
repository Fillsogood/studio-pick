package org.example.studiopick.application.admin.dto.workshop;

import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.workshop.WorkShop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 워크샵 관련 DTO들
 */
public class WorkshopDTOs {

public record AdminWorkshopListResponse(
        List<AdminWorkshopResponse> workshops,
        AdminWorkshopPaginationResponse pagination
) {
    public record AdminWorkshopResponse(
            Long id,
            String address,
            String title,
            String description,
            String status, // PENDING, ACTIVE, INACTIVE, REJECTED
            String studioName,
            String instructorName,
            BigDecimal price,
            int currentParticipants,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}

public record AdminWorkshopPaginationResponse(
        int currentPage,
        long totalElements,
        int totalPages
) {}

public record AdminWorkshopDetailResponse(
        Long id,
        String title,
        String description,
        String status,
        AdminWorkshopStatsInfo statsInfo,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record AdminWorkshopStatsInfo(
            long totalParticipants,
            long totalReservations,
            long completedReservations,
            long cancelledReservations
    ) {}
}

public record AdminWorkshopApprovalCommand(
    WorkShopStatus action, // APPROVE, REJECT
    String reason
) {}

public record AdminWorkshopApprovalResponse(
        Long workshopId,
        String workshopTitle,
        WorkShopStatus action,
        String reason,
        LocalDateTime processedAt
) {}

public record AdminWorkshopStatusCommand(
        WorkShopStatus status, // ACTIVE, INACTIVE, SUSPENDED
        String reason
) {}

public record AdminWorkshopStatusResponse(
        Long workshopId,
        String workshopTitle,
        WorkShopStatus oldStatus,
        WorkShopStatus newStatus,
        String reason,
        LocalDateTime changedAt
) {}

public record AdminWorkshopStatsResponse(
        long totalWorkshops,
        long activeWorkshops,
        long pendingWorkshops,
        long rejectedWorkshops,
        WorkshopCategoryStats categoryStats,
        WorkshopPerformanceStats performanceStats
) {
    public record WorkshopCategoryStats(
            List<CategoryCount> categoryCounts
    ) {
        public record CategoryCount(String category, long count) {}
    }
    
    public record WorkshopPerformanceStats(
            BigDecimal totalRevenue,
            BigDecimal averagePrice,
            long totalParticipants
    ) {}
}

public record AdminPopularWorkshopResponse(
        String period,
        List<PopularWorkshop> workshops
) {
    public record PopularWorkshop(
            Long id,
            String title,
            String studioName,
            long participantCount,
            BigDecimal revenue
    ) {}
}
    public record ReportedWorkshopDto(
        Long workshopId,
        String title,
        String instructor,
        String ownerName,
        int totalReportCount,
        List<String> reportReasons,
        String hideStatus,
        LocalDateTime createdAt
    ) {}
}
