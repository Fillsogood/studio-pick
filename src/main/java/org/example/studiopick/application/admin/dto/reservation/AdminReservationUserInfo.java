package org.example.studiopick.application.admin.dto.reservation;

public record AdminReservationUserInfo(
    Long id,
    String name,
    String email,
    String phone
) {}