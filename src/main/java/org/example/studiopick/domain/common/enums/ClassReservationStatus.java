package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ClassReservationStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled");

    private final String value;

    ClassReservationStatus(String value) {
        this.value = value;
    }

}
