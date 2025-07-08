package org.example.studiopick.application.reservation.dto;

public record ReservationCancelRequest(
    Long userId,
    String reason  // 취소 사유 (optional)
) {}
