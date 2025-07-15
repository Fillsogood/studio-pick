package org.example.studiopick.application.reservation.dto;

import jakarta.validation.constraints.Size;

/**
 * 예약 취소 요청 Command (Controller에서 받는 사용자 입력)
 */
public record ReservationCancelCommand(
    @Size(max = 500, message = "취소 사유는 500자를 초과할 수 없습니다")
    String cancelReason  // 취소 사유 (선택사항)
) {}
