package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum StudioStatus {
    PENDING("pending"),
    APPROVED("approved"),
    INACTIVE("inactive");

    private final String value;

    StudioStatus(String value) {
        this.value = value;
    }

}
