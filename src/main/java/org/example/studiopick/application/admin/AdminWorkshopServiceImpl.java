package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.example.studiopick.application.admin.dto.workshop.WorkshopDTOs.*;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.HideStatus;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.domain.workshop.WorkShopImage;
import org.example.studiopick.infrastructure.report.ReportRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 워크샵 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminWorkshopServiceImpl implements AdminWorkshopService {

    private final JpaWorkShopRepository workshopRepository;
    private final PaginationValidator paginationValidator;
    private final ReportRepository reportRepository;

    @Override
    public AdminWorkshopListResponse getWorkshops(int page, Integer size, String status, String keyword) {
        Pageable pageable = PageRequest.of(paginationValidator.validatePage(page), paginationValidator.validateSize(size));

        Page<WorkShop> workshops;

        if (status != null && !status.isBlank()) {
            HideStatus hideStatus = HideStatus.valueOf(status.toUpperCase());
            if (keyword != null && !keyword.isBlank()) {
                workshops = workshopRepository.findByHideStatusAndTitleContaining(hideStatus, keyword, pageable);
            } else {
                workshops = workshopRepository.findByHideStatus(hideStatus, pageable);
            }
        } else {
            if (keyword != null && !keyword.isBlank()) {
                workshops = workshopRepository.findByTitleContaining(keyword, pageable);
            } else {
                workshops = workshopRepository.findAll(pageable);
            }
        }
        List<AdminWorkshopListResponse.AdminWorkshopResponse> responses = workshops.getContent().stream()
            .map(w -> new AdminWorkshopListResponse.AdminWorkshopResponse(
                w.getId(),
                w.getTitle(),
                w.getDescription(),
                w.getHideStatus().name(),
                w.getOwner().getName(),
                w.getInstructor(),
                w.getPrice(),
                w.getReservations().size(),
                w.getCreatedAt(),
                w.getUpdatedAt()
            ))
            .toList();

        AdminWorkshopPaginationResponse pagination = new AdminWorkshopPaginationResponse(
            page,
            workshops.getTotalElements(),
            workshops.getTotalPages()
        );

        return new AdminWorkshopListResponse(responses, pagination);
    }

    @Override
    public AdminWorkshopDetailResponse getWorkshopDetail(Long workshopId) {
        WorkShop w = workshopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("워크샵을 찾을 수 없습니다."));

        // 실제 데이터 계산
        int totalParticipants = w.getReservations().size();
        int totalReservations = w.getReservations().size();
        int completedReservations = calculateCompletedReservations(w);
        int cancelledReservations = calculateCancelledReservations(w);
        
        AdminWorkshopDetailResponse.AdminWorkshopStatsInfo statsInfo = new AdminWorkshopDetailResponse.AdminWorkshopStatsInfo(
            totalParticipants,
            totalReservations,
            completedReservations,
            cancelledReservations
        );

        List<String> imageUrls = w.getImages().stream()
            .map(WorkShopImage::getImageUrl)
            .toList();

        return new AdminWorkshopDetailResponse(
            w.getId(),
            w.getTitle(),
            w.getDescription(),
            w.getHideStatus().name(),
            statsInfo,
            imageUrls,
            w.getCreatedAt(),
            w.getUpdatedAt()
        );
    }


    @Override
    @Transactional
    public AdminWorkshopApprovalResponse approveWorkshop(Long workshopId, AdminWorkshopApprovalCommand command) {
        WorkShop workshop = workshopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("워크샵을 찾을 수 없습니다."));

        String action = command.action();
        if ("APPROVE".equalsIgnoreCase(action)) {
            workshop.open();
        } else if ("REJECT".equalsIgnoreCase(action)) {
            workshop.close();
        } else {
            throw new IllegalArgumentException("지원하지 않는 액션입니다: " + action);
        }

        return new AdminWorkshopApprovalResponse(
            workshop.getId(),
            workshop.getTitle(),
            action,
            command.reason(),
            LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public AdminWorkshopStatusResponse changeWorkshopStatus(Long workshopId, AdminWorkshopStatusCommand command) {
        WorkShop workshop = workshopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("워크샵을 찾을 수 없습니다."));

        String oldStatus = workshop.getHideStatus().name();
        workshop.WorkShopChangeStatus(HideStatus.valueOf(command.status().toUpperCase()));

        return new AdminWorkshopStatusResponse(
            workshop.getId(),
            workshop.getTitle(),
            oldStatus,
            workshop.getHideStatus().name(),
            command.reason(),
            LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void deleteWorkshop(Long workshopId, String reason) {
        WorkShop workshop = workshopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("워크샵을 찾을 수 없습니다."));
        workshopRepository.delete(workshop);
    }

    @Override
    public AdminWorkshopStatsResponse getWorkshopStats() {
        long total = workshopRepository.count();
        long active = workshopRepository.countByHideStatus(HideStatus.OPEN);
        long pending = workshopRepository.countByHideStatus(HideStatus.CLOSED);
        long rejected = workshopRepository.countByHideStatus(HideStatus.REPORTED);

        // 실제 성과 데이터 계산
        BigDecimal totalRevenue = calculateTotalWorkshopRevenue();
        BigDecimal averageRevenue = calculateAverageWorkshopRevenue();
        long totalParticipants = calculateTotalWorkshopParticipants();
        
        return new AdminWorkshopStatsResponse(
            total,
            active,
            pending,
            rejected,
            new AdminWorkshopStatsResponse.WorkshopCategoryStats(List.of()),
            new AdminWorkshopStatsResponse.WorkshopPerformanceStats(
                totalRevenue, averageRevenue, totalParticipants
            )
        );
    }

    @Override
    public AdminPopularWorkshopResponse getPopularWorkshops(String period, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<WorkShop> workshops = workshopRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AdminPopularWorkshopResponse.PopularWorkshop> result = workshops.stream()
            .map(w -> {
                // 실제 참가자 수와 매출 계산
                long participantCount = calculateWorkshopParticipants(w);
                BigDecimal revenue = calculateWorkshopRevenue(w);
                
                return new AdminPopularWorkshopResponse.PopularWorkshop(
                    w.getId(),
                    w.getTitle(),
                    w.getOwner().getName(),
                    participantCount,
                    revenue
                );
            }).toList();

        return new AdminPopularWorkshopResponse(period, result);
    }


    @Override
    public List<ReportedWorkshopDto> getReportedWorkshops(int page, Integer size) {
        Pageable pageable = PageRequest.of(
            paginationValidator.validatePage(page),
            paginationValidator.validateSize(size)
        );

        Page<WorkShop> workshops = workshopRepository.findByHideStatus(HideStatus.REPORTED, pageable);

        Pageable top5Pageable = PageRequest.of(0, 5);

        return workshops.stream().map(w -> {
            long reportCount = reportRepository.countByReportedTypeAndReportedId(ReportType.CLASS, w.getId());
            List<String> reasons = reportRepository
                .findByReportedTypeAndReportedIdOrderByCreatedAtDesc(ReportType.CLASS, w.getId(), top5Pageable)
                .stream()
                .map(Report::getReason)
                .toList();

            return new ReportedWorkshopDto(
                w.getId(),
                w.getTitle(),
                w.getInstructor(),
                w.getOwner().getName(),
                (int) reportCount,
                reasons,
                w.getHideStatus().name(),
                w.getCreatedAt()
            );
        }).toList();
    }
    
    // ============ Private Helper Methods ============
    
    /**
     * 워크샵의 완료된 예약 수 계산
     */
    private int calculateCompletedReservations(WorkShop workshop) {
        return (int) workshop.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
            .count();
    }
    
    /**
     * 워크샵의 취소된 예약 수 계산
     */
    private int calculateCancelledReservations(WorkShop workshop) {
        return (int) workshop.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.CANCELLED || 
                         r.getStatus() == ReservationStatus.CANCEL_REQUESTED)
            .count();
    }
    
    /**
     * 전체 워크샵 매출 계산
     */
    private BigDecimal calculateTotalWorkshopRevenue() {
        List<WorkShop> workshops = workshopRepository.findAll();
        return workshops.stream()
            .map(this::calculateWorkshopRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 워크샵 평균 매출 계산
     */
    private BigDecimal calculateAverageWorkshopRevenue() {
        long totalWorkshops = workshopRepository.count();
        if (totalWorkshops == 0) return BigDecimal.ZERO;
        
        BigDecimal totalRevenue = calculateTotalWorkshopRevenue();
        return totalRevenue.divide(BigDecimal.valueOf(totalWorkshops), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 전체 워크샵 참가자 수 계산
     */
    private long calculateTotalWorkshopParticipants() {
        List<WorkShop> workshops = workshopRepository.findAll();
        return workshops.stream()
            .mapToLong(this::calculateWorkshopParticipants)
            .sum();
    }
    
    /**
     * 특정 워크샵의 참가자 수 계산
     */
    private long calculateWorkshopParticipants(WorkShop workshop) {
        return workshop.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
            .mapToLong(r -> r.getPeopleCount() != null ? r.getPeopleCount() : 1)
            .sum();
    }
    
    /**
     * 특정 워크샵의 매출 계산
     */
    private BigDecimal calculateWorkshopRevenue(WorkShop workshop) {
        return workshop.getReservations().stream()
            .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
            .map(r -> BigDecimal.valueOf(r.getTotalAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
