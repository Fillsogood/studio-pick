package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed"),
    REFUNDED("refunded"); // 환불 처리까지 완료된 상태

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

}
