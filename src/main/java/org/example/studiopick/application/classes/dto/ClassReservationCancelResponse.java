package org.example.studiopick.application.classes.dto;

public record ClassReservationCancelResponse(
    Long reservationId,
    boolean refunded,
    String message
) {}
