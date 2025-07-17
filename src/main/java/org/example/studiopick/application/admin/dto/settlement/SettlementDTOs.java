package org.example.studiopick.application.admin.dto.settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 정산 관련 기본 DTO 클래스들 정의

public class SettlementDTOs {

public record AdminSettlementListResponse(
    java.util.List<AdminSettlementTargetDto> settlements,
    AdminSettlementPaginationResponse pagination
) {}

public record AdminSettlementTargetDto(
    Long id, String targetName, String ownerName, BigDecimal totalAmount, 
    BigDecimal platformFee, BigDecimal payoutAmount, String status, 
    LocalDateTime createdAt, LocalDateTime settledAt
) {}

public record AdminSettlementPaginationResponse(int currentPage, long totalElements, int totalPages) {}

public record AdminSettlementDetailResponse(
    Long id, String studioName, BigDecimal amount, String status, String details
) {}

public record AdminSettlementProcessCommand(String action, String reason) {}

public record AdminSettlementProcessResponse(Long id, String action, LocalDateTime processedAt) {}

public record AdminBulkSettlementCommand(java.util.List<Long> settlementIds, String action) {}

public record AdminBulkSettlementResponse(
    int totalRequested, int successCount, int failureCount, 
    BigDecimal totalAmount, java.util.List<String> errors
) {}

public record AdminSettlementStatsResponse(
    String startDate, String endDate,
    BigDecimal totalAmount, BigDecimal processedAmount, BigDecimal pendingAmount,
    long totalCount, long processedCount, long pendingCount
) {}

public record AdminStudioSettlementResponse(
    Long studioId, String studioName,
    java.util.List<AdminStudioSettlementDto> settlements,
    AdminSettlementPaginationResponse pagination
) {}

public record AdminStudioSettlementDto(
    Long id, BigDecimal totalAmount, BigDecimal platformFee, 
    BigDecimal payoutAmount, String status, LocalDateTime createdAt, LocalDateTime settledAt
) {}

public record AdminWorkshopSettlementResponse(
    Long workshopId, String workshopTitle,
    java.util.List<AdminWorkshopSettlementDto> settlements,
    AdminSettlementPaginationResponse pagination
) {}

public record AdminWorkshopSettlementDto(
    Long id, BigDecimal totalAmount, BigDecimal platformFee, 
    BigDecimal payoutAmount, String status, LocalDateTime createdAt, LocalDateTime settledAt
) {}

public record AdminCommissionRateResponse(
    BigDecimal defaultRate,
    java.util.List<AdminCommissionRateDto> categoryRates
) {}

public record AdminCommissionRateDto(
    String category, String name, BigDecimal rate, boolean isActive
) {}

public record AdminCommissionRateUpdateCommand(String category, BigDecimal rate) {}

public record AdminSettlementReportCommand(String startDate, String endDate, String format) {}

public record AdminSettlementReportResponse(
    String reportId, String startDate, String endDate, 
    String reportUrl, String status, LocalDateTime generatedAt
) {}

public record AdminSettlementApprovalCommand(String action, String reason) {}

public record AdminSettlementApprovalResponse(
    Long settlementId, String action, String reason, LocalDateTime processedAt
) {}
}
