package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.ReportType;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.WorkShopStatus;
import org.example.studiopick.domain.report.Report;
import org.example.studiopick.domain.user.User;
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

import static org.example.studiopick.application.admin.dto.workshop.WorkshopDTOs.*;

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
            WorkShopStatus workShopStatus = WorkShopStatus.valueOf(status.toUpperCase());
            if (keyword != null && !keyword.isBlank()) {
                workshops = workshopRepository.findByStatusAndTitleContaining(workShopStatus, keyword, pageable);
            } else {
                workshops = workshopRepository.findByStatus(workShopStatus, pageable);
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
                        w.getStatus().name(),
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

        int totalParticipants = w.getReservations().size();
        int totalReservations = w.getReservations().size();
        int completedReservations = calculateCompletedReservations(w);
        int cancelledReservations = calculateCancelledReservations(w);

        AdminWorkshopDetailResponse.AdminWorkshopStatsInfo statsInfo = new AdminWorkshopDetailResponse.AdminWorkshopStatsInfo(
                totalParticipants, totalReservations, completedReservations, cancelledReservations
        );

        List<String> imageUrls = w.getImages().stream()
                .map(WorkShopImage::getImageUrl)
                .toList();

        return new AdminWorkshopDetailResponse(
                w.getId(),
                w.getTitle(),
                w.getDescription(),
                w.getStatus().name(),
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

        WorkShopStatus action = command.action();
        if (WorkShopStatus.ACTIVE.equals(action)) {
            workshop.activate();
            User owner = workshop.getOwner();
            if (!owner.getRole().equals(UserRole.WORKSHOP_OWNER)) {
                owner.changeRole(UserRole.WORKSHOP_OWNER);  // 혹은 WORKSHOP_OWNER
            }
            if (!owner.isWorkshopOwner()) {
                owner.setWorkshopOwner(true);
            }
        } else if (WorkShopStatus.INACTIVE.equals(action)) {
            workshop.deactivate();
            // 비활성화시 권한을 강등시킬지 여부는 정책에 따라
        }
        else {
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

        WorkShopStatus oldStatus = workshop.getStatus();
        workshop.changeStatus(command.status());

        return new AdminWorkshopStatusResponse(
                workshop.getId(),
                workshop.getTitle(),
                oldStatus,
                workshop.getStatus(),
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
        long active = workshopRepository.countByStatus(WorkShopStatus.ACTIVE);
        long pending = workshopRepository.countByStatus(WorkShopStatus.PENDING);
        long inactive = workshopRepository.countByStatus(WorkShopStatus.INACTIVE);

        BigDecimal totalRevenue = calculateTotalWorkshopRevenue();
        BigDecimal averageRevenue = calculateAverageWorkshopRevenue();
        long totalParticipants = calculateTotalWorkshopParticipants();

        return new AdminWorkshopStatsResponse(
                total,
                active,
                pending,
                inactive,
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

        Page<WorkShop> workshops = workshopRepository.findByStatus(WorkShopStatus.INACTIVE, pageable);

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
                    w.getStatus().name(),
                    w.getCreatedAt()
            );
        }).toList();
    }

    private int calculateCompletedReservations(WorkShop workshop) {
        return (int) workshop.getReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .count();
    }

    private int calculateCancelledReservations(WorkShop workshop) {
        return (int) workshop.getReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED ||
                        r.getStatus() == ReservationStatus.CANCEL_REQUESTED)
                .count();
    }

    private BigDecimal calculateTotalWorkshopRevenue() {
        List<WorkShop> workshops = workshopRepository.findAll();
        return workshops.stream()
                .map(this::calculateWorkshopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageWorkshopRevenue() {
        long totalWorkshops = workshopRepository.count();
        if (totalWorkshops == 0) return BigDecimal.ZERO;
        BigDecimal totalRevenue = calculateTotalWorkshopRevenue();
        return totalRevenue.divide(BigDecimal.valueOf(totalWorkshops), 2, BigDecimal.ROUND_HALF_UP);
    }

    private long calculateTotalWorkshopParticipants() {
        List<WorkShop> workshops = workshopRepository.findAll();
        return workshops.stream()
                .mapToLong(this::calculateWorkshopParticipants)
                .sum();
    }

    private long calculateWorkshopParticipants(WorkShop workshop) {
        return workshop.getReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .mapToLong(r -> r.getPeopleCount() != null ? r.getPeopleCount() : 1)
                .sum();
    }

    private BigDecimal calculateWorkshopRevenue(WorkShop workshop) {
        return workshop.getReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED)
                .map(r -> BigDecimal.valueOf(r.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
