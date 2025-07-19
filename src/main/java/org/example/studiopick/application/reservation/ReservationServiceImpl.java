package org.example.studiopick.application.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.refund.RefundService;
import org.example.studiopick.application.reservation.dto.*;
import org.example.studiopick.common.util.SystemSettingUtils;
import org.example.studiopick.common.validator.PaginationValidator;
import org.example.studiopick.common.validator.UserValidator;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.reservation.Reservation;
import org.example.studiopick.domain.reservation.ReservationDomainService;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.domain.workshop.WorkShop;
import org.example.studiopick.infrastructure.User.JpaUserRepository;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.reservation.mybatis.ReservationSearchMapper;
import org.example.studiopick.infrastructure.reservation.mybatis.dto.UserReservationSearchCriteria;
import org.example.studiopick.infrastructure.studio.JpaStudioRepository;
import org.example.studiopick.infrastructure.workshop.JpaWorkShopRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 예약 서비스 구현체
 * - JPA: 기본 CRUD 및 핵심 비즈니스 로직
 * - MyBatis: 복잡한 검색 쿼리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    
    // ✅ JPA Repository (기본 CRUD + 핵심 로직)
    private final JpaReservationRepository jpaReservationRepository;
    private final JpaWorkShopRepository jpaWorkShopRepository;
    
    // ✅ MyBatis Mapper (복잡한 검색)
    private final ReservationSearchMapper reservationSearchMapper;
    
    private final ReservationDomainService reservationDomainService;
    private final JpaStudioRepository jpaStudioRepository;
    private final JpaUserRepository jpaUserRepository;
    private final UserValidator userValidator;
    private final PaginationValidator paginationValidator;
    private final SystemSettingUtils settingUtils;
    
    // RefundService 주입 (순환 의존성 해결)
    private final RefundService refundService;

    @Override
    public UserReservationDetailResponse getReservationDetail(Long reservationId, Long userId) {
        // 1. 예약 조회 및 존재 확인
        Reservation reservation = jpaReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 2. 본인 예약 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예약만 조회할 수 있습니다.");
        }

        // 3. 스튜디오 or 클래스 타입 분기
        String type;
        UserReservationDetailResponse.StudioInfo studioInfo = null;
        UserReservationDetailResponse.WorkshopInfo workshopInfo = null;

        if (reservation.getStudio() != null) {
            // 스튜디오 예약일 경우
            type = "studio";
            Studio studio = reservation.getStudio();

            studioInfo = new UserReservationDetailResponse.StudioInfo(
                    studio.getId(),
                    studio.getName(),
                    studio.getPhone(),
                    studio.getLocation(),
                    studio.getHourlyBaseRate(),
                    studio.getPerPersonRate()
            );
        } else if (reservation.getWorkshop() != null) {
            type = "workshop";
            WorkShop workshop = reservation.getWorkshop();

            workshopInfo = new UserReservationDetailResponse.WorkshopInfo(
                    workshop.getId(),
                    workshop.getTitle(),
                    workshop.getInstructor(),
                    workshop.getAddress(),
                    workshop.getThumbnailUrl(),
                    workshop.getDate(),
                    workshop.getStartTime(),
                    workshop.getEndTime(),
                    workshop.getPrice()
            );
        } else {
            throw new IllegalStateException("예약에는 studio 또는 workshop 중 하나는 반드시 존재해야 합니다.");
        }

        // 4. 최종 응답 생성
        return new UserReservationDetailResponse(
                reservation.getId(),
                type,
                studioInfo,
                workshopInfo,
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


    @Override
    public ReservationResponse createStudioReservation(Long studioId, ReservationCreateCommand command, Long userId) {
        // 1. 기본 유효성 검증
        validateReservationRules(command);

        // 2. 스튜디오 존재 확인 (락 제거 - 성능 개선)
        Studio studio = jpaStudioRepository.findById(studioId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Studio id를 찾을 수 없습니다."));

        // 3. 사용자 존재 확인
        User user = jpaUserRepository.findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("해당 User id를 찾을 수 없습니다."));

        // 4. 예약 시간 중복 검증 (CONFIRMED 상태만 체크)
        reservationDomainService.validateOverlapping(
            command.studioId(),
            command.reservationDate(),
            ReservationStatus.CONFIRMED,  // PENDING이 아닌 CONFIRMED만 체크
            command.startTime(),
            command.endTime()
        );

        // 5. 요금 계산 및 최소 금액 검증
        Long totalAmount = calculateTotalAmount(studio, command.startTime(), command.endTime(), command.peopleCount());
        validateMinimumAmount(totalAmount);

        // 6. 예약 생성
        Reservation reservation = Reservation.builder()
            .studio(studio)
            .user(user)
            .reservationDate(command.reservationDate())
            .startTime(command.startTime())
            .endTime(command.endTime())
            .status(ReservationStatus.PENDING)
            .peopleCount(command.peopleCount())
            .totalAmount(totalAmount)
            .build();

        try {
            Reservation saved = jpaReservationRepository.save(reservation);

            log.info("예약 생성 완료: reservationId={}, userId={}, studioId={}",
                saved.getId(), command.userId(), studioId);

            return new ReservationResponse(
                saved.getId(),
                saved.getTotalAmount(),
                saved.getStatus()
            );
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("해당 시간대에 이미 예약이 존재합니다.", e);
        }
    }

    @Override
    public ReservationResponse createWorkshopReservation(Long workshopId, ReservationCreateCommand command, Long userId) {
        validateReservationRules(command);
        WorkShop workshop = jpaWorkShopRepository.findById(workshopId)
            .orElseThrow(() -> new IllegalArgumentException("해당 Workshop id를 찾을 수 없습니다."));
        User user = jpaUserRepository.findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("해당 User id를 찾을 수 없습니다."));
        if (!workshop.isAvailableForReservation()) {
            throw new IllegalStateException("현재 예약이 불가능한 공방입니다.");
        }
        if (!workshop.isValidTimeRange()) {
            throw new IllegalStateException("공방의 시작시간과 종료시간이 유효하지 않습니다.");
        }
        Reservation reservation = Reservation.builder()
            .workShop(workshop)
            .user(user)
            .reservationDate(workshop.getDate())
            .startTime(workshop.getStartTime())
            .endTime(workshop.getEndTime())
            .status(ReservationStatus.PENDING)
            .peopleCount(command.peopleCount())
            .totalAmount(workshop.getPrice().longValue())
            .build();
        try {
            Reservation saved = jpaReservationRepository.save(reservation);
            log.info("공방 예약 생성 완료: reservationId={}, userId={}, workshopId={}",
                saved.getId(), command.userId(), workshopId);
            return new ReservationResponse(
                saved.getId(),
                saved.getTotalAmount(),
                saved.getStatus()
            );
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("공방 예약 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public AvailableTimesResponse getAvailableTimes(Long studioId, LocalDate date) {
        
        // ✅ JPA의 단순한 조회 사용
        List<Reservation> reservations = jpaReservationRepository.findByStudioIdAndReservationDateAndStatus(
            studioId, date, ReservationStatus.CONFIRMED
        );

        // 예약된 시간 리스트
        List<String> bookedTimes = reservations.stream()
            .sorted(Comparator.comparing(Reservation::getStartTime))
            .map(r -> r.getStartTime().toString())
            .toList();

        // 시스템 설정에서 운영시간 조회
        int startHour = settingUtils.getIntegerSetting("studio.operating.start.hour", 9);
        int endHour = settingUtils.getIntegerSetting("studio.operating.end.hour", 18);

        LocalTime start = LocalTime.of(startHour, 0);
        LocalTime end = LocalTime.of(endHour, 0);

        List<String> allTimes = new ArrayList<>();
        LocalTime cursor = start;
        while (cursor.isBefore(end)) {
            allTimes.add(cursor.toString());
            cursor = cursor.plusHours(1);
        }

        // 예약된 시간을 제외한 가용 시간
        List<String> availableTimes = allTimes.stream()
            .filter(t -> !bookedTimes.contains(t))
            .collect(Collectors.toList());

        return new AvailableTimesResponse(
            availableTimes,
            bookedTimes
        );
    }

    @Override
    public UserReservationListResponse getUserReservations(
            Long userId, int page, int size, String status, 
            LocalDate startDate, LocalDate endDate, Long studioId) {
        
        // 입력값 검증
        paginationValidator.validatePaginationParameters(page, size);
        userValidator.findAndValidateUser(userId);

        // ✅ MyBatis를 사용한 복잡한 필터링 (복잡한 if-else 분기 제거!)
        UserReservationSearchCriteria criteria = UserReservationSearchCriteria.of(
            userId, status, startDate, endDate, studioId, page, size);

        List<UserReservationResponse> reservations = reservationSearchMapper.searchUserReservations(criteria);
        long totalCount = reservationSearchMapper.countUserReservations(criteria);

        return new UserReservationListResponse(
            reservations,
            new PaginationResponse(page, totalCount)
        );
    }

    /**
     * 결제 완료 시 예약 확정
     */
    @Override
    @Transactional
    public void confirmReservationPayment(Long reservationId) {
        Reservation reservation = jpaReservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("예약 상태가 PENDING이 아닙니다.");
        }

        reservation.confirm();
        jpaReservationRepository.save(reservation);

        log.info("예약 결제 확정 완료: reservationId={}", reservationId);
    }

    @Override
    @Transactional
    public ReservationCancelResponse cancelReservation(Long id, ReservationCancelRequest request) {
        
        // 1. 예약 조회 및 존재 확인
        Reservation reservation = jpaReservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("예약 Id를 찾을 수 없습니다."));

        // 2. 본인 예약 확인
        if (!reservation.getUser().getId().equals(request.userId())) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        // 3. 취소 가능 상태 확인
        if (!reservation.isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다: " + reservation.getStatus());
        }

        // 4. 취소 가능 시간 검증
        // ✅ 12시간 이내에는 취소 자체가 불가능
        LocalDateTime reservationDateTime = reservation.getReservationDate().atTime(reservation.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        
        // 예약 시작 시간이 이미 지났다면 취소 불가
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalStateException("이미 시작된 예약은 취소할 수 없습니다.");
        }
        
        // 12시간 이내에는 취소 불가 (환불도 불가하므로)
        long hoursUntilReservation = Duration.between(now, reservationDateTime).toHours();
        if (hoursUntilReservation < 12) {
            throw new IllegalStateException("예약 시작 12시간 전까지만 취소 가능합니다. (현재 " + hoursUntilReservation + "시간 전)");
        }

        // 5. 원래 예약 상태 저장 (취소 전에 미리 저장)
        ReservationStatus originalStatus = reservation.getStatus();
        
        // 6. 취소 수수료 계산
        RefundInfo refundInfo = calculateRefundAmount(reservation, reservationDateTime);

        // 7. 예약 취소 처리
        reservation.cancelWithoutValidation(request.reason());
        Reservation saved = jpaReservationRepository.save(reservation);

        // 8. 자동 환불 처리 (원래 상태가 CONFIRMED였던 경우만)
        if (originalStatus == ReservationStatus.CONFIRMED) {
            try {
                // 환불 처리 (항상 환불 금액이 존재함)
                refundService.processRefundForReservation(reservation, refundInfo, request.reason());
                
                // 환불 성공 시 예약 상태를 REFUNDED로 변경
                saved.refund();
                saved = jpaReservationRepository.save(saved);
                
                log.info("자동 환불 처리 완료: reservationId={}, refundAmount={}",
                    id, refundInfo.refundAmount());
                    
            } catch (Throwable e) {  // 모든 예외를 잡아서 트랜잭션 롤백 방지
                // ✅ JSON 파싱 에러 특별 처리
                if (e.getMessage() != null && 
                    (e.getMessage().contains("JSON decoding error") || 
                     e.getMessage().contains("Cannot deserialize") ||
                     e.getMessage().contains("LocalDateTime"))) {
                    log.error("환불 처리 실패 - 토스페이먼츠 응답 파싱 오류: reservationId={}, error={}", 
                        id, "토스페이먼츠 API 응답 형식 변경으로 인한 파싱 실패: " + e.getMessage());
                } else {
                    log.error("환불 처리 실패 - 예약 취소는 유지: reservationId={}, error={}", id, e.getMessage());
                }
                // 환불 실패해도 예약 취소는 유지 (관리자가 수동 처리 가능)
                // 예외를 다시 던지지 않아 트랜잭션 롤백 방지
            }
        }

        log.info("예약 취소 요청 완료: reservationId={}, userId={}, reason={}, refundAmount={}, finalStatus={}", 
                id, request.userId(), request.reason(), refundInfo.refundAmount(), saved.getStatus());

        // 9. 응답 생성
        return new ReservationCancelResponse(
            saved.getId(),
            saved.getStatus(),
            LocalDateTime.now()
        );
    }

    // Private helper methods
    
    /**
     * ✅ 새로운 비즈니스 룰에 따른 취소 수수료 및 환불 금액 계산
     * - 24시간 전까지 : 전액환불
     * - 12시간 전까지 : 50% 환불
     * - 12시간 이내 : 취소 불가 (이 메서드에 도달하지 않음)
     */
    private RefundInfo calculateRefundAmount(Reservation reservation, LocalDateTime reservationDateTime) {
        BigDecimal originalAmount = BigDecimal.valueOf(reservation.getTotalAmount());
        
        // 현재 시각과 예약 시각 간의 시간 차이 계산
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilReservation = Duration.between(now, reservationDateTime).toHours();
        
        BigDecimal cancellationFee;
        BigDecimal refundAmount;
        String feePolicy;
        
        if (hoursUntilReservation >= 24) {
            // 24시간 전까지 - 전액환불
            cancellationFee = BigDecimal.ZERO;
            refundAmount = originalAmount;
            feePolicy = "24시간 전 취소 - 전액환불";
            
        } else {
            // 12시간 이상 24시간 미만 - 50% 환불
            // (이 로직에 도달하는 경우는 12시간 이상이 보장됨)
            refundAmount = originalAmount.multiply(BigDecimal.valueOf(0.5));
            cancellationFee = originalAmount.subtract(refundAmount);
            feePolicy = "12시간 전 취소 - 50% 환불";
        }
        
        log.info("환불 정책 적용: reservationId={}, hoursUntil={}, policy={}, refundAmount={}", 
            reservation.getId(), hoursUntilReservation, feePolicy, refundAmount);
        
        return RefundInfo.of(originalAmount, cancellationFee, feePolicy);
    }

    // 기존 helper methods
    private void validateReservationRules(ReservationCreateCommand command) {
        // 1. 인원 수 검증 (Short -> int 변환)
        if (!reservationDomainService.isValidPeopleCount(command.peopleCount().intValue())) {
            int maxPeople = settingUtils.getIntegerSetting("reservation.max.people", 20);
            throw new IllegalArgumentException("예약 인원은 1명 이상 " + maxPeople + "명 이하여야 합니다.");
        }

        // 2. 미래 날짜 검증
        if (!reservationDomainService.isValidAdvanceReservation(command.reservationDate())) {
            int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
            throw new IllegalArgumentException("예약은 최대 " + maxAdvanceDays + "일 후까지만 가능합니다.");
        }

        // 3. 과거 날짜 검증
        if (command.reservationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜로는 예약할 수 없습니다.");
        }

        // 4. 예약 시간 길이 검증
        if (!reservationDomainService.isValidReservationDuration(command.startTime(), command.endTime())) {
            int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);
            int maxHours = settingUtils.getIntegerSetting("reservation.max.hours", 8);
            throw new IllegalArgumentException("예약 시간은 " + minHours + "시간 이상 " + maxHours + "시간 이하여야 합니다.");
        }
    }

    private void validateMinimumAmount(Long totalAmount) {
        int minAmount = settingUtils.getIntegerSetting("payment.min.amount", 10000);

        if (totalAmount < minAmount) {
            throw new IllegalArgumentException("최소 결제 금액은 " + minAmount + "원 입니다.");
        }
    }

    private Long calculateTotalAmount(Studio studio, LocalTime startTime, LocalTime endTime, Short peopleCount) {
        // 사용 시간 계산
        long hours = Duration.between(startTime, endTime).toHours();

        // 기본 요금 × 시간
        long baseAmount = studio.getHourlyBaseRate() * hours;

        // 인원 수 × 인당 요금 × 시간
        long personAmount = peopleCount * studio.getPerPersonRate() * hours;

        return baseAmount + personAmount;
    }


}
