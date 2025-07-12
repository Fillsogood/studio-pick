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

    public static FavoriteType from(String value) {
        for (FavoriteType type : FavoriteType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 즐겨찾기 타입입니다: " + value);
    }

}
