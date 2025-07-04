package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum FavoriteType {
    STUDIO("studio"),
    CLASS("class");

    private final String value;

    FavoriteType(String value) {
        this.value = value;
    }

}
