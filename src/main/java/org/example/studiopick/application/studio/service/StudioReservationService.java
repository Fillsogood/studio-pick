package org.example.studiopick.application.studio.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioReservationResponseDto;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.domain.common.enums.Weekday;
import org.example.studiopick.domain.studio.Studio;
import org.example.studiopick.domain.studio.StudioOperatingHours;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.example.studiopick.infrastructure.studio.JpaStudioOperatingHoursRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudioReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaStudioOperatingHoursRepository studioOperatingHoursRepository;


    public List<StudioReservationResponseDto> getReservations(Long studioId, LocalDate date, String status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        ReservationStatus statusEnum = ReservationStatus.valueOf(status.toUpperCase());

//        Page<Reservation> reservationPage = reservationRepository
//                .findByStudioIdAndReservationDateAndStatus(studioId, date, statusEnum, pageRequest);

//        return reservationPage.map(reservation -> StudioReservationResponseDto.builder()
//                .reservationId(reservation.getId())
//                .userName(reservation.getUser().getName())
//                .userPhone(reservation.getUser().getPhone())
//                .reservationDate(reservation.getReservationDate())
//                .startTime(reservation.getStartTime())
//                .endTime(reservation.getEndTime())
//                .peopleCount(reservation.getPeopleCount())
//                .status(reservation.getStatus().name())
//                .totalAmount(reservation.getTotalAmount())
//                .build()).toList();
//    }
        return null;
    }

    // 🔹 30분 이내 예약 가능한 스튜디오 조회
    public List<Studio> getAvailableStudiosWithin30Minutes() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();
        LocalTime limitTime = currentTime.plusMinutes(30);

        // ✅ DayOfWeek → Weekday enum 변환
        Weekday weekday = Weekday.fromDayOfWeek(today);

        // 오늘 요일 기준, 현재 시간 ~ 30분 내 오픈 상태인 스튜디오 필터링
        List<StudioOperatingHours> operatingHoursList =
                studioOperatingHoursRepository.findByWeekday(weekday);

        return operatingHoursList.stream()
                .filter(op -> {
                    LocalTime open = op.getOpenTime();
                    LocalTime close = op.getCloseTime();
                    return !currentTime.isAfter(close) && !limitTime.isBefore(open);
                })
                .map(StudioOperatingHours::getStudio)
                .distinct()
                .collect(Collectors.toList());
    }

}

