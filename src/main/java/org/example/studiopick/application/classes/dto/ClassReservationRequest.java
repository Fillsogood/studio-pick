package org.example.studiopick.application.classes.dto;

public record ClassReservationRequest(
    Long userId,
    int participants
) {}
