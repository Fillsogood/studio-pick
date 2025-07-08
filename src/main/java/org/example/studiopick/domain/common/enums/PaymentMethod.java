package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CARD("card"),
    KAKAOPAY("kakaopay"),
    TOSSPAY("tosspay");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

}
