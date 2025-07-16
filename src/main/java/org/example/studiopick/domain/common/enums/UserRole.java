package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("user"),
    STUDIO_OWNER("studio_owner"),
    WORKSHOP_OWNER("workshop_owner"),
    ADMIN("admin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

}
