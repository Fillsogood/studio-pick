package org.example.studiopick.application.studio.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.dto.StudioReservationResponseDto;
import org.example.studiopick.domain.common.enums.ReservationStatus;
import org.example.studiopick.infrastructure.reservation.JpaReservationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudioReservationService {

    private final JpaReservationRepository reservationRepository;

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
}
