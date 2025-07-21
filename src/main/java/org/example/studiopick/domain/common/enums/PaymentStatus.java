package org.example.studiopick.domain.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum PaymentStatus {
    READY("READY"),           // 결제 대기 (토스 추가)
    IN_PROGRESS("in_progress"), // 결제 진행중 (토스 추가)
    WAITING_FOR_DEPOSIT("waiting_for_deposit"), // 입금 대기 (토스 추가)
    DONE("DONE"),             // 결제 완료 (토스에서는 DONE)
    PAID("PAID"),             // 우리 시스템 완료
    CANCELLED("CANCELLED"),
    PARTIAL_CANCELED("partial_canceled"), // 부분 취소 (토스 추가)
    ABORTED("aborted"),       // 결제 중단 (토스 추가)
    EXPIRED("expired"),       // 결제 만료 (토스 추가)
    REFUNDED("REFUNDED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static PaymentStatus fromValue(String value) {
        return Arrays.stream(values())
            .filter(s -> s.getValue().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
    }
}
