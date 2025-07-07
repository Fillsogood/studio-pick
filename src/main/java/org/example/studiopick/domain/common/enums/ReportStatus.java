package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("pending"),
    AUTO_HIDDEN("auto_hidden"),
    REVIEWED("reviewed"),
    RESTORED("restored"),
    DELETED("deleted");

    private final String value;

    ReportStatus(String value) {
        this.value = value;
    }

}
