package org.example.studiopick.domain.common.enums;

import lombok.Getter;

import java.time.DayOfWeek;

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

    // ✅ DayOfWeek → Weekday enum 변환 메서드
    public static Weekday fromDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> Weekday.MON;
            case TUESDAY -> Weekday.TUE;
            case WEDNESDAY -> Weekday.WED;
            case THURSDAY -> Weekday.THU;
            case FRIDAY -> Weekday.FRI;
            case SATURDAY -> Weekday.SAT;
            case SUNDAY -> Weekday.SUN;
        };
    }

}
