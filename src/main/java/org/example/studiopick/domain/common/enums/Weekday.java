package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum Weekday {
    MON("mon"),
    TUE("tue"),
    WED("wed"),
    THU("thu"),
    FRI("fri"),
    SAT("sat"),
    SUN("sun");

    private final String value;

    Weekday(String value) {
        this.value = value;
    }

}
