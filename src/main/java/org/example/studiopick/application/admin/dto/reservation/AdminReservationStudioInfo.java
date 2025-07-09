package org.example.studiopick.application.admin.dto.reservation;

public record AdminReservationStudioInfo(
    Long id,
    String name,
    String phone,
    String location
) {}