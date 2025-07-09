package org.example.studiopick.application.payment.dto;

import org.example.studiopick.application.reservation.dto.PaginationResponse;

import java.util.List;

public record UserPaymentHistoryListResponse(
    List<UserPaymentHistoryResponse> payments,
    PaginationResponse pagination
) {}
