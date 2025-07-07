package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReviewStatus {
    VISIBLE("visible"),
    HIDDEN("hidden"),
    DELETED("deleted");

    private final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

}
