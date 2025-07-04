package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReportType {
    ARTWORK("artwork"),
    CLASS("class"),
    REVIEW("review");

    private final String value;

    ReportType(String value) {
        this.value = value;
    }

}
