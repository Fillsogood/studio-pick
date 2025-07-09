package org.example.studiopick.application.classes.dto;

import java.math.BigDecimal;

public record ClassReservationResponse(
    Long reservationId,
    String classTitle,
    String instructor,
    int participants,
    BigDecimal totalAmount,
    String status
) {}
