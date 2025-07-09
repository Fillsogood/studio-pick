package org.example.studiopick.application.admin.dto.reservation;

public record AdminReservationStatsResponse(
    long totalReservations,
    long pendingReservations,
    long confirmedReservations,
    long cancelledReservations,
    long completedReservations,
    long refundedReservations,
    long todayReservations
) {}