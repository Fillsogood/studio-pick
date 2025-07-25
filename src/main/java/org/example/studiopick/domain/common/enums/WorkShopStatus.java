package org.example.studiopick.domain.common.enums;

public enum WorkShopStatus {

    PENDING("pending"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    HIDE("hide");

    private final String value;

    WorkShopStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
