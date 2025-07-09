package org.example.studiopick.application.classes.dto;

import java.util.List;

public record UserClassReservationListResponse(
    List<UserClassReservationDto> reservations
) {}
