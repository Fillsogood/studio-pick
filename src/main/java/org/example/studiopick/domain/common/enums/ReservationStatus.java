package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("PENDING"),
    CONFIRMED("CONFIRMED"),
    CANCEL_REQUESTED("CANCEL_REQUESTED"),
    CANCELLED("CANCELLED"),
    COMPLETED("COMPLETED"),
    REFUNDED("REFUNDED");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

}
