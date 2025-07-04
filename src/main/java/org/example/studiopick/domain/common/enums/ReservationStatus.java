package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

}
