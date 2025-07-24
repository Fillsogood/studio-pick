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

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final JpaReservationRepository jpaReservationRepository;
    private final JpaWorkShopRepository jpaWorkShopRepository;
    private final ReservationSearchMapper reservationSearchMapper;
    private final ReservationDomainService reservationDomainService;
    private final JpaStudioRepository jpaStudioRepository;
    private final JpaUserRepository jpaUserRepository;
    private final UserValidator userValidator;
    private final PaginationValidator paginationValidator;
    private final SystemSettingUtils settingUtils;
    private final RefundService refundService;

    @Override
    public UserReservationDetailResponse getReservationDetail(Long reservationId, Long userId) {
        Reservation reservation = jpaReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 예약만 조회할 수 있습니다.");
        }

        String type;
        UserReservationDetailResponse.StudioInfo studioInfo = null;
        UserReservationDetailResponse.WorkshopInfo workshopInfo = null;

        if (reservation.getStudio() != null) {
            type = "studio";
            Studio studio = reservation.getStudio();
            studioInfo = new UserReservationDetailResponse.StudioInfo(
                    studio.getId(), studio.getName(), studio.getPhone(), studio.getLocation(),
                    studio.getHourlyBaseRate(), studio.getPerPersonRate());
        } else if (reservation.getWorkshop() != null) {
            type = "workshop";
            WorkShop workshop = reservation.getWorkshop();
            workshopInfo = new UserReservationDetailResponse.WorkshopInfo(
                    workshop.getId(), workshop.getTitle(), workshop.getInstructor(),
                    workshop.getAddress(), workshop.getThumbnailUrl(), workshop.getDate(),
                    workshop.getStartTime(), workshop.getEndTime(), workshop.getPrice());
        } else {
            throw new IllegalStateException("예약에는 studio 또는 workshop 중 하나는 반드시 존재해야 합니다.");
        }

        return new UserReservationDetailResponse(
                reservation.getId(), type, studioInfo, workshopInfo,
                reservation.getReservationDate(), reservation.getStartTime(), reservation.getEndTime(),
                reservation.getPeopleCount(), reservation.getTotalAmount(), reservation.getStatus().getValue(),
                reservation.getCancelledReason(), reservation.getCancelledAt(),
                reservation.getCreatedAt(), reservation.getUpdatedAt());
    }

    @Override
    @Transactional
    public ReservationResponse createStudioReservation(Long studioId, ReservationCreateCommand command, Long userId) {
        validateReservationRules(command);
        Studio studio = jpaStudioRepository.findById(studioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 Studio id를 찾을 수 없습니다."));
        User user = jpaUserRepository.findById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 User id를 찾을 수 없습니다."));
        reservationDomainService.validateOverlapping(
                command.studioId(), command.reservationDate(), ReservationStatus.CONFIRMED,
                command.startTime(), command.endTime());
        Long totalAmount = calculateTotalAmount(studio, command.startTime(), command.endTime(), command.peopleCount());
        validateMinimumAmount(totalAmount);

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
            return new ReservationResponse(saved.getId(), saved.getTotalAmount(), saved.getStatus());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("해당 시간대에 이미 예약이 존재합니다.", e);
        }
    }

    @Override
    @Transactional
    public ReservationResponse createWorkshopReservation(Long workshopId, ReservationCreateCommand command, Long userId) {
        validateWorkshopReservationRules(command);
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
                .peopleCount((short) 1)
                .totalAmount(workshop.getPrice().longValue())
                .build();
        try {
            Reservation saved = jpaReservationRepository.save(reservation);
            log.info("공방 예약 생성 완료: reservationId={}, userId={}, workshopId={}",
                    saved.getId(), command.userId(), workshopId);
            return new ReservationResponse(saved.getId(), saved.getTotalAmount(), saved.getStatus());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("공방 예약 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public AvailableTimesResponse getAvailableTimes(Long studioId, LocalDate date) {
        List<Reservation> reservations = jpaReservationRepository.findByStudioIdAndReservationDateAndStatus(
                studioId, date, ReservationStatus.CONFIRMED);
        List<String> bookedTimes = reservations.stream()
                .sorted(Comparator.comparing(Reservation::getStartTime))
                .map(r -> r.getStartTime().toString())
                .toList();

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

        List<String> availableTimes = allTimes.stream()
                .filter(t -> !bookedTimes.contains(t))
                .collect(Collectors.toList());

        return new AvailableTimesResponse(availableTimes, bookedTimes);
    }

    @Override
    public UserReservationListResponse getUserReservations(Long userId, int page, int size,
                                                           String status, LocalDate startDate,
                                                           LocalDate endDate, Long studioId) {
        paginationValidator.validatePaginationParameters(page, size);
        userValidator.findAndValidateUser(userId);

        UserReservationSearchCriteria criteria = UserReservationSearchCriteria.of(
                userId, status, startDate, endDate, studioId, page, size);

        List<UserReservationResponse> reservations = reservationSearchMapper.searchUserReservations(criteria);
        long totalCount = reservationSearchMapper.countUserReservations(criteria);

        return new UserReservationListResponse(reservations, new PaginationResponse(page, totalCount));
    }

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
        Reservation reservation = jpaReservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 Id를 찾을 수 없습니다."));

        if (!reservation.getUser().getId().equals(request.userId())) {
            throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
        }

        if (!reservation.isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 예약 상태입니다: " + reservation.getStatus());
        }

        LocalDateTime reservationDateTime = reservation.getReservationDate().atTime(reservation.getStartTime());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 시작된 예약은 취소할 수 없습니다.");
        }

        if (Duration.between(LocalDateTime.now(), reservationDateTime).toHours() < 12) {
            throw new IllegalStateException("예약 시작 12시간 전까지만 취소 가능합니다.");
        }

        ReservationStatus originalStatus = reservation.getStatus();
        RefundInfo refundInfo = calculateRefundAmount(reservation, reservationDateTime);

        if (originalStatus == ReservationStatus.CONFIRMED) {
            try {
                refundService.processRefundForReservation(reservation, refundInfo, request.reason());
                return new ReservationCancelResponse(reservation.getId(), reservation.getStatus(), LocalDateTime.now());
            } catch (Throwable e) {
                log.error("자동 환불 처리 실패: reservationId={}, error={}", id, e.getMessage());
                return new ReservationCancelResponse(reservation.getId(), reservation.getStatus(), LocalDateTime.now());
            }
        } else {
            reservation.cancelWithoutValidation(request.reason());
            Reservation saved = jpaReservationRepository.save(reservation);
            log.info("예약 취소 완료: reservationId={}, status={}", saved.getId(), saved.getStatus());
            return new ReservationCancelResponse(saved.getId(), saved.getStatus(), LocalDateTime.now());
        }
    }

    private RefundInfo calculateRefundAmount(Reservation reservation, LocalDateTime reservationDateTime) {
        BigDecimal originalAmount = BigDecimal.valueOf(reservation.getTotalAmount());
        LocalDateTime now = LocalDateTime.now();
        long hoursUntilReservation = Duration.between(now, reservationDateTime).toHours();
        BigDecimal cancellationFee;
        BigDecimal refundAmount;
        String feePolicy;

        if (hoursUntilReservation >= 24) {
            cancellationFee = BigDecimal.ZERO;
            refundAmount = originalAmount;
            feePolicy = "24시간 전 취소 - 전액환불";
        } else {
            refundAmount = originalAmount.multiply(BigDecimal.valueOf(0.5));
            cancellationFee = originalAmount.subtract(refundAmount);
            feePolicy = "12시간 전 취소 - 50% 환불";
        }

        log.info("환불 정책 적용: reservationId={}, hoursUntil={}, policy={}, refundAmount={}",
                reservation.getId(), hoursUntilReservation, feePolicy, refundAmount);
        return RefundInfo.of(originalAmount, cancellationFee, feePolicy);
    }

    private void validateReservationRules(ReservationCreateCommand command) {
        if (!reservationDomainService.isValidPeopleCount(command.peopleCount().intValue())) {
            int maxPeople = settingUtils.getIntegerSetting("reservation.max.people", 20);
            throw new IllegalArgumentException("예약 인원은 1명 이상 " + maxPeople + "명 이하여야 합니다.");
        }
        if (!reservationDomainService.isValidAdvanceReservation(command.reservationDate())) {
            int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
            throw new IllegalArgumentException("예약은 최대 " + maxAdvanceDays + "일 후까지만 가능합니다.");
        }
        if (command.reservationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜로는 예약할 수 없습니다.");
        }
        if (!reservationDomainService.isValidReservationDuration(command.startTime(), command.endTime())) {
            int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);
            throw new IllegalArgumentException("예약 시간은 " + minHours + "시간 이상이어야 합니다.");
        }
    }

    private void validateWorkshopReservationRules(ReservationCreateCommand command) {
        if (!reservationDomainService.isValidAdvanceReservation(command.reservationDate())) {
            int maxAdvanceDays = settingUtils.getIntegerSetting("reservation.advance.days", 90);
            throw new IllegalArgumentException("예약은 최대 " + maxAdvanceDays + "일 후까지만 가능합니다.");
        }
        if (command.reservationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜로는 예약할 수 없습니다.");
        }
        if (!reservationDomainService.isValidReservationDuration(command.startTime(), command.endTime())) {
            int minHours = settingUtils.getIntegerSetting("reservation.min.hours", 1);
            throw new IllegalArgumentException("예약 시간은 " + minHours + "시간 이상이어야 합니다.");
        }
    }

    private void validateMinimumAmount(Long totalAmount) {
        int minAmount = settingUtils.getIntegerSetting("payment.min.amount", 10000);
        if (totalAmount < minAmount) {
            throw new IllegalArgumentException("최소 결제 금액은 " + minAmount + "원 입니다.");
        }
    }

    private Long calculateTotalAmount(Studio studio, LocalTime startTime, LocalTime endTime, Short peopleCount) {
        long hours = Duration.between(startTime, endTime).toHours();
        long baseAmount = studio.getHourlyBaseRate() * hours;
        long personAmount = peopleCount * studio.getPerPersonRate() * hours;
        return baseAmount + personAmount;
    }
}
