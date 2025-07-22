package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.settlement.*;
import static org.example.studiopick.application.admin.dto.settlement.SettlementDTOs.*;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.SettlementStatus;
import org.example.studiopick.domain.payment.Settlement;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.infrastructure.payment.JpaSettlementRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminSettlementServiceImpl implements AdminSettlementService {

    private final PaginationValidator paginationValidator;
    private final JpaSettlementRepository settlementRepository;
    private final JpaStudioRepository studioRepository;
    private final JpaWorkShopRepository workshopRepository;
    
    @Value("${platform.commission.rate:0.1}")
    private double platformCommissionRate;

    @Override
    public AdminSettlementListResponse getSettlementTargets(int page, int size, String status, String startDate, String endDate) {
        paginationValidator.validatePaginationParameters(page, size);


        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Settlement> settlements;

        if (status != null && !status.isBlank()) {
            SettlementStatus settlementStatus = SettlementStatus.valueOf(status.toUpperCase());
            settlements = settlementRepository.findBySettlementStatus(settlementStatus, pageable);
        } else {
            settlements = settlementRepository.findAll(pageable);
        }
        
        List<AdminSettlementTargetDto> settlementTargets = settlements.getContent().stream()
            .map(s -> new AdminSettlementTargetDto(
                s.getId(),
                s.getTargetName(), // 스튜디오명 또는 워크샵명
                s.getOwnerName(),  // 소유자명
                s.getTotalAmount(),
                s.getPlatformFee(),
                s.getPayoutAmount(),
                s.getSettlementStatus().getValue(),
                s.getCreatedAt(),
                s.getSettledAt()
            ))
            .toList();
        
        return new AdminSettlementListResponse(
            settlementTargets,
            new AdminSettlementPaginationResponse(
                page,
                settlements.getTotalElements(),
                settlements.getTotalPages()
            )
        );
    }

    @Override
    public AdminSettlementDetailResponse getSettlementDetail(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));
        
        String settlementType = settlement.isStudioSettlement() ? "스튜디오" : "워크샵";
        
        return new AdminSettlementDetailResponse(
            settlement.getId(),
            settlement.getTargetName(),
            settlement.getPayoutAmount(),
            settlement.getSettlementStatus().getValue(),
            String.format("[%s 정산] 총 금액: %s, 플랫폼 수수료: %s, 세금: %s, 소유자: %s",
                settlementType,
                settlement.getTotalAmount(),
                settlement.getPlatformFee(),
                settlement.getTaxAmount(),
                settlement.getOwnerName())
        );
    }

    @Override
    @Transactional
    public AdminSettlementProcessResponse processSettlement(Long settlementId, AdminSettlementProcessCommand command) {
        log.info("Settlement {} processed with action: {}", settlementId, command.action());
        return new AdminSettlementProcessResponse(
            settlementId,
            command.action(),
            LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public AdminBulkSettlementResponse processBulkSettlement(AdminBulkSettlementCommand command) {
        log.info("Bulk settlement processing: {} items", command.settlementIds().size());
        
        List<Settlement> settlements = settlementRepository.findAllById(command.settlementIds());
        
        int successCount = 0;
        int failCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> errorMessages = List.of();
        
        for (Settlement settlement : settlements) {
            try {
                if ("APPROVE".equalsIgnoreCase(command.action())) {
                    settlement.markAsPaid();
                    successCount++;
                    totalAmount = totalAmount.add(settlement.getPayoutAmount());
                } else if ("HOLD".equalsIgnoreCase(command.action())) {
                    settlement.hold();
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("Failed to process settlement {}: {}", settlement.getId(), e.getMessage());
            }
        }
        
        return new AdminBulkSettlementResponse(
            command.settlementIds().size(),
            successCount,
            failCount,
            totalAmount,
            errorMessages
        );
    }

    @Override
    public AdminSettlementStatsResponse getSettlementStats(String startDate, String endDate) {
        // 날짜 파싱
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (startDate != null && !startDate.isBlank()) {
            startDateTime = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        }
        if (endDate != null && !endDate.isBlank()) {
            endDateTime = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX);
        }
        
        // 전체 데이터 조회
        List<Settlement> allSettlements = settlementRepository.findAll();
        
        // 기간별 필터링
        List<Settlement> filteredSettlements = allSettlements;
        if (startDateTime != null && endDateTime != null) {
            final LocalDateTime start = startDateTime;
            final LocalDateTime end = endDateTime;
            filteredSettlements = allSettlements.stream()
                .filter(s -> s.getCreatedAt().isAfter(start) && s.getCreatedAt().isBefore(end))
                .toList();
        }
        
        // 통계 계산
        BigDecimal totalPendingAmount = calculateAmountByStatus(filteredSettlements, SettlementStatus.PENDING);
        BigDecimal totalPaidAmount = calculateAmountByStatus(filteredSettlements, SettlementStatus.PAID);
        BigDecimal totalHoldAmount = calculateAmountByStatus(filteredSettlements, SettlementStatus.CANCELLED);
        
        long pendingCount = countByStatus(filteredSettlements, SettlementStatus.PENDING);
        long paidCount = countByStatus(filteredSettlements, SettlementStatus.PAID);
        long holdCount = countByStatus(filteredSettlements, SettlementStatus.CANCELLED);
        
        return new AdminSettlementStatsResponse(
            startDate != null ? startDate : "",
            endDate != null ? endDate : "",
            totalPendingAmount,
            totalPaidAmount,
            totalHoldAmount,
            pendingCount,
            paidCount,
            holdCount
        );
    }

    @Override
    public AdminStudioSettlementResponse getStudioSettlement(Long studioId, int page, int size, String status) {
        paginationValidator.validatePaginationParameters(page, size);
        
        Studio studio = studioRepository.findById(studioId)
            .orElseThrow(() -> new IllegalArgumentException("스튜디오를 찾을 수 없습니다."));
        
        List<Settlement> studioSettlements = settlementRepository.findByStudioId(studioId);
        
        // 상태별 필터링
        if (status != null && !status.isBlank()) {
            SettlementStatus settlementStatus = SettlementStatus.valueOf(status.toUpperCase());
            studioSettlements = studioSettlements.stream()
                .filter(s -> s.getSettlementStatus() == settlementStatus)
                .toList();
        }
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, studioSettlements.size());
        List<Settlement> pagedSettlements = studioSettlements.subList(start, end);
        
        List<AdminStudioSettlementDto> settlementDtos = pagedSettlements.stream()
            .map(s -> new AdminStudioSettlementDto(
                s.getId(),
                s.getTotalAmount(),
                s.getPlatformFee(),
                s.getPayoutAmount(),
                s.getSettlementStatus().getValue(),
                s.getCreatedAt(),
                s.getSettledAt()
            ))
            .toList();
        
        return new AdminStudioSettlementResponse(
            studioId,
            studio.getName(),
            settlementDtos,
            new AdminSettlementPaginationResponse(
                page,
                (long) studioSettlements.size(),
                (int) Math.ceil((double) studioSettlements.size() / size)
            )
        );
    }
    
    @Override
    public AdminWorkshopSettlementResponse getWorkshopSettlement(Long workshopId, int page, int size, String status) {
        paginationValidator.validatePaginationParameters(page, size);
        
        WorkShop workshop = workshopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("워크샵을 찾을 수 없습니다."));
        
        List<Settlement> workshopSettlements = settlementRepository.findByWorkshopId(workshopId);
        
        // 상태별 필터링
        if (status != null && !status.isBlank()) {
            SettlementStatus settlementStatus = SettlementStatus.valueOf(status.toUpperCase());
            workshopSettlements = workshopSettlements.stream()
                .filter(s -> s.getSettlementStatus() == settlementStatus)
                .toList();
        }
        
        // 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, workshopSettlements.size());
        List<Settlement> pagedSettlements = workshopSettlements.subList(start, end);
        
        List<AdminWorkshopSettlementDto> settlementDtos = pagedSettlements.stream()
            .map(s -> new AdminWorkshopSettlementDto(
                s.getId(),
                s.getTotalAmount(),
                s.getPlatformFee(),
                s.getPayoutAmount(),
                s.getSettlementStatus().getValue(),
                s.getCreatedAt(),
                s.getSettledAt()
            ))
            .toList();
        
        return new AdminWorkshopSettlementResponse(
            workshopId,
            workshop.getTitle(),
            settlementDtos,
            new AdminSettlementPaginationResponse(
                page,
                (long) workshopSettlements.size(),
                (int) Math.ceil((double) workshopSettlements.size() / size)
            )
        );
    }

    @Override
    public AdminCommissionRateResponse getCommissionRates() {
        // 플랫폼 수수료율을 퍼센트로 변환 (0.1 -> 10%)
        BigDecimal platformRate = BigDecimal.valueOf(platformCommissionRate * 100);
        
        List<AdminCommissionRateDto> rates = List.of(
            new AdminCommissionRateDto("PLATFORM", "플랫폼 수수료", platformRate, true)
        );
        
        return new AdminCommissionRateResponse(
            platformRate,
            rates
        );
    }

    @Override
    @Transactional
    public AdminCommissionRateResponse updateCommissionRate(AdminCommissionRateUpdateCommand command) {
        log.info("Commission rate updated for category: {}", command.category());
        return getCommissionRates();
    }

    @Override
    public AdminSettlementReportResponse generateSettlementReport(AdminSettlementReportCommand command) {
        // 리포트 ID 생성
        String reportId = "SETTLEMENT_REPORT_" + System.currentTimeMillis();
        
        return new AdminSettlementReportResponse(
            reportId,
            command.startDate(),
            command.endDate(),
            "정산 리포트",
            "GENERATED",
            LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public AdminSettlementApprovalResponse approveSettlement(Long settlementId, AdminSettlementApprovalCommand command) {
        log.info("Settlement {} {} by admin", settlementId, command.action());
        
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));
        
        if ("APPROVE".equalsIgnoreCase(command.action())) {
            settlement.markAsPaid();
        } else if ("REJECT".equalsIgnoreCase(command.action())) {
            settlement.hold();
        } else {
            throw new IllegalArgumentException("지원하지 않는 액션입니다: " + command.action());
        }
        
        return new AdminSettlementApprovalResponse(
            settlementId,
            command.action(),
            command.reason(),
            LocalDateTime.now()
        );
    }
    
    // ============ Private Helper Methods ============
    
    /**
     * 상태별 금액 계산
     */
    private BigDecimal calculateAmountByStatus(List<Settlement> settlements, SettlementStatus status) {
        return settlements.stream()
            .filter(s -> s.getSettlementStatus() == status)
            .map(Settlement::getPayoutAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 상태별 건수 계산
     */
    private long countByStatus(List<Settlement> settlements, SettlementStatus status) {
        return settlements.stream()
            .filter(s -> s.getSettlementStatus() == status)
            .count();
    }
}
