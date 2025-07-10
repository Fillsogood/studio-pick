package org.example.studiopick.application.studio.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class StudioReservationResponseDto {

    private Long reservationId;
    private String userName;
    private String userPhone;
    private LocalDate reservationDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Short peopleCount;
    private String status;
    private Long totalAmount;
}

