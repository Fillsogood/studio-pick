package org.example.studiopick.application.reservation.dto;

import java.util.List;

public record UserReservationListResponse(
    List<UserReservationResponse> reservations,
    PaginationResponse pagination
) {}