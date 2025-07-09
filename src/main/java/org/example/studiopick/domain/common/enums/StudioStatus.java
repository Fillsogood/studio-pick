package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum StudioStatus {
    PENDING("pending"), //승인 대기
    APPROVED("approved"),
    INACTIVE("inactive"), // 비활성화
    ACTIVE("active"),         // 활성화 (승인 완료) - 관리자용 추가
    SUSPENDED("suspended"),   // 정지 - 관리자용 추가
    REJECTED("rejected");     // 승인 거부 - 관리자용 추가
    private final String value;

    StudioStatus(String value) {
        this.value = value;
    }

}
