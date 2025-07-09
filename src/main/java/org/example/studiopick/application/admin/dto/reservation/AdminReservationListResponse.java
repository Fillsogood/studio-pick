package org.example.studiopick.application.admin.dto.reservation;

import java.util.List;

public record AdminReservationListResponse(
    List<AdminReservationResponse> reservations,
    AdminReservationPaginationResponse pagination
) {}