package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ClassStatus {
    OPEN("open"),
    CLOSED("closed"),
    REPORTED("reported");

    private final String value;

    ClassStatus(String value) {
        this.value = value;
    }

}
