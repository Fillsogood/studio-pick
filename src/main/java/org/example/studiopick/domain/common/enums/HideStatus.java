package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum HideStatus {
    OPEN("open"),
    CLOSED("closed"),
    REPORTED("reported");

    private final String value;

    HideStatus(String value) {
        this.value = value;
    }

}
