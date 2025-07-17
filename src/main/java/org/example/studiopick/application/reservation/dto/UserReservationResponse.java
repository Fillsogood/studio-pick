package org.example.studiopick.application.reservation.dto;

public record UserReservationResponse(
    Long id,              // Reservation ID
    String type,           // studio or workshop
    String studioName,      // Studio.name
    String workshopTitle,   // Workshop.title
    String instructor,
    String date,            // reservationDate (YYYY-MM-DD)
    String startTime,       // startTime (HH:mm)
    String endTime,         // endTime (HH:mm)
    String status,          // ReservationStatus.name()
    Long totalAmount             // totalAmount
) {}
