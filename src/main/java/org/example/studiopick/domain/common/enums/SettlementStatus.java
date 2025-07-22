package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum SettlementStatus {
    PENDING("pending"),
    PAID("paid"),
    CANCELLED("cancelled");

    private final String value;

    SettlementStatus(String value) {
        this.value = value;
    }

}
