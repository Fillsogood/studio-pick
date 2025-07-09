package org.example.studiopick.application.reservation.dto;

public record CancelReservationCommand (
        Long reservationId,
        Long userId
){}
