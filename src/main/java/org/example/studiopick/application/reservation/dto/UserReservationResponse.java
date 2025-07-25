package org.example.studiopick.application.reservation.dto;

public record UserReservationResponse(
    Long id,              // Reservation ID
    String type,           // studio or workshop
    String studioName,      // Studio.name
    String workshopTitle,   // Workshop.title
    String instructor,      // nullable 가능해야 함
    String date,            // reservationDate (YYYY-MM-DD)
    String startTime,       // startTime (HH:mm)
    String endTime,         // endTime (HH:mm)
    String status,          // ReservationStatus.name()
    Long totalAmount,       // totalAmount
    String studioImageUrl,  // 스튜디오 썸네일 이미지 URL
    String workshopImageUrl // 워크샵 썸네일 이미지 URL
) {}
