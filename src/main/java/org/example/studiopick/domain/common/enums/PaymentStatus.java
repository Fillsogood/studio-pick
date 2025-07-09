package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PAID("paid"),
    CANCELLED("cancelled"),
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

}
