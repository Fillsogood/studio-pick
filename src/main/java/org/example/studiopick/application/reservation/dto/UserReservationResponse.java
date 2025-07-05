package org.example.studiopick.application.reservation.dto;

public record UserReservationResponse(
    Long id,              // Reservation ID
    String studioName,      // Studio.name
    String date,            // reservationDate (YYYY-MM-DD)
    String startTime,       // startTime (HH:mm)
    String endTime,         // endTime (HH:mm)
    String status,          // ReservationStatus.name()
    Long totalAmount             // totalAmount
) {}
