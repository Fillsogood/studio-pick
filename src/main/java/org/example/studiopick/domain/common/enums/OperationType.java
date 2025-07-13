package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    SPACE_RENTAL("space_rental", "공간 대여"),
    CLASS_WORKSHOP("class_workshop", "공방 체험");

    private final String value;
    private final String description;

    OperationType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
