package org.example.studiopick.application.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.dto.reservation.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.reservation.mybatis.ReservationSearchMapper;
import org.example.studiopick.infrastructure.reservation.mybatis.dto.ReservationSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 예약 서비스 구현체
 * - JPA: 기본 CRUD 및 핵심 비즈니스 로직  
 * - MyBatis: 복잡한 검색 쿼리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminReservationServiceImpl implements AdminReservationService {
    
    // ✅ JPA Repository (기본 CRUD + 핵심 로직)
    private final JpaReservationRepository jpaReservationRepository;
    
    // ✅ MyBatis Mapper (복잡한 검색)
    private final ReservationSearchMapper reservationSearchMapper;
    
    private final ReservationDomainService reservationDomainService;
    private final PaginationValidator paginationValidator;
    private final SystemSettingUtils settingUtils;

    @Override
    public AdminReservationListResponse getAllReservations(
            int page, Integer size, String status, String startDate, String endDate,
            Long userId, Long studioId, String searchKeyword) {
        
        // 입력값 검증
        int pageSize = size != null ? size : settingUtils.getIntegerSetting("pagination.default.size", 10);
        paginationValidator.validatePaginationParameters(page, pageSize);
        
        // ✅ MyBatis를 사용한 복잡한 검색 (복잡한 if-else 분기 완전 제거!)
        ReservationSearchCriteria criteria = ReservationSearchCriteria.of(
            status, startDate, endDate, userId, studioId, searchKeyword, page, pageSize);
        
        List<AdminReservationResponse> reservations = reservationSearchMapper.searchReservations(criteria);
        long totalCount = reservationSearchMapper.countReservations(criteria);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        return new AdminReservationListResponse(
            reservations,
            new AdminReservationPaginationResponse(page, totalCount, totalPages)
        );
    }

    @Override
    public AdminReservationDetailResponse getReservationDetail(Long reservationId) {
        Reservation reservation = jpaReservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        
        return toAdminReservationDetailResponse(reservation);
    }

    @Override
    @Transactional
    public AdminReservationStatusResponse changeReservationStatus(
            Long reservationId, AdminReservationStatusCommand command) {
        
        Reservation reservation = jpaReservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        
        ReservationStatus oldStatus = reservation.getStatus();
        ReservationStatus newStatus = parseReservationStatus(command.status());
        
        // 상태 변경 유효성 검증
        validateStatusChange(oldStatus, newStatus);
        
        try {
            // 상태별 처리
            switch (newStatus) {
                case CONFIRMED -> reservation.confirm();
                case CANCELLED -> {
                    reservation.changeStatus(ReservationStatus.CANCELLED);
                    reservation.updateCancelInfo(command.reason());
                }
                case REFUNDED -> reservation.changeStatus(ReservationStatus.REFUNDED);
                case COMPLETED -> reservation.complete();
                default -> reservation.changeStatus(newStatus);
            }
            
            jpaReservationRepository.save(reservation);
            
            log.info("예약 상태 변경 완료: reservationId={}, {} -> {}, reason={}",
                reservationId, oldStatus, newStatus, command.reason());
            
            return new AdminReservationStatusResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getStudio().getName(),
                oldStatus.getValue(),
                newStatus.getValue(),
                command.reason(),
                LocalDateTime.now()
            );
            
        } catch (Exception e) {
            log.error("예약 상태 변경 실패: reservationId={}, error={}", reservationId, e.getMessage());
            throw new RuntimeException("예약 상태 변경에 실패했습니다.", e);
        }
    }

    @Override
    public AdminReservationStatsResponse getReservationStats() {
        // ✅ JPA의 간단한 집계 메서드 사용
        return new AdminReservationStatsResponse(
            jpaReservationRepository.count(), // 전체
            jpaReservationRepository.countByStatus(ReservationStatus.PENDING),
            jpaReservationRepository.countByStatus(ReservationStatus.CONFIRMED),
            jpaReservationRepository.countByStatus(ReservationStatus.CANCELLED),
            jpaReservationRepository.countByStatus(ReservationStatus.COMPLETED),
            jpaReservationRepository.countByStatus(ReservationStatus.REFUNDED),
            jpaReservationRepository.countByReservationDate(LocalDate.now())
        );
    }

    @Override
    public AdminReservationListResponse getUserReservations(
            Long userId, int page, int size, String status) {
        
        paginationValidator.validatePaginationParameters(page, size);
        
        // ✅ MyBatis를 사용한 사용자별 검색
        ReservationSearchCriteria criteria = ReservationSearchCriteria.of(
            status, null, null, userId, null, null, page, size);
        
        List<AdminReservationResponse> reservations = reservationSearchMapper.searchReservations(criteria);
        long totalCount = reservationSearchMapper.countReservations(criteria);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        return new AdminReservationListResponse(
            reservations,
            new AdminReservationPaginationResponse(page, totalCount, totalPages)
        );
    }

    @Override
    public AdminReservationListResponse getStudioReservations(
            Long studioId, int page, int size, String status) {
        
        paginationValidator.validatePaginationParameters(page, size);
        
        // ✅ MyBatis를 사용한 스튜디오별 검색
        ReservationSearchCriteria criteria = ReservationSearchCriteria.of(
            status, null, null, null, studioId, null, page, size);
        
        List<AdminReservationResponse> reservations = reservationSearchMapper.searchReservations(criteria);
        long totalCount = reservationSearchMapper.countReservations(criteria);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        return new AdminReservationListResponse(
            reservations,
            new AdminReservationPaginationResponse(page, totalCount, totalPages)
        );
    }

    // Private helper methods
    private void validateStatusChange(ReservationStatus oldStatus, ReservationStatus newStatus) {
        if (oldStatus == newStatus) {
            throw new IllegalArgumentException("동일한 상태로 변경할 수 없습니다.");
        }
        
        if (oldStatus == ReservationStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 예약은 상태를 변경할 수 없습니다.");
        }
        
        if (oldStatus == ReservationStatus.REFUNDED) {
            throw new IllegalArgumentException("환불된 예약은 상태를 변경할 수 없습니다.");
        }
    }

    private ReservationStatus parseReservationStatus(String status) {
        try {
            return ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 예약 상태입니다: " + status);
        }
    }

    private AdminReservationDetailResponse toAdminReservationDetailResponse(Reservation reservation) {
        return new AdminReservationDetailResponse(
            reservation.getId(),
            new AdminReservationUserInfo(
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getUser().getEmail(),
                reservation.getUser().getPhone()
            ),
            new AdminReservationStudioInfo(
                reservation.getStudio().getId(),
                reservation.getStudio().getName(),
                reservation.getStudio().getPhone(),
                reservation.getStudio().getLocation()
            ),
            reservation.getReservationDate(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getPeopleCount(),
            reservation.getTotalAmount(),
            reservation.getStatus().getValue(),
            reservation.getCancelledReason(),
            reservation.getCancelledAt(),
            reservation.getCreatedAt(),
            reservation.getUpdatedAt()
        );
    }
}
