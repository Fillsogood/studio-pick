package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ArtworkStatus {
    PUBLIC("public"),
    PRIVATE("private"),
    REPORTED("reported");

    private final String value;

    ArtworkStatus(String value) {
        this.value = value;
    }

}
