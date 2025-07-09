package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    READY("ready"),           // 결제 대기 (토스 추가)
    IN_PROGRESS("in_progress"), // 결제 진행중 (토스 추가)
    WAITING_FOR_DEPOSIT("waiting_for_deposit"), // 입금 대기 (토스 추가)
    DONE("done"),             // 결제 완료 (토스에서는 DONE)
    PAID("paid"),             // 우리 시스템 완료
    CANCELLED("cancelled"),
    PARTIAL_CANCELED("partial_canceled"), // 부분 취소 (토스 추가)
    ABORTED("aborted"),       // 결제 중단 (토스 추가)
    EXPIRED("expired"),       // 결제 만료 (토스 추가)
    REFUNDED("refunded");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

}
