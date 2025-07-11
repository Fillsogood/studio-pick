package org.example.studiopick.domain.common.enums;

/**
 * 환불 상태
 */
public enum RefundStatus {
    PENDING("환불 대기"),
    PROCESSING("환불 처리 중"),
    COMPLETED("환불 완료"),
    FAILED("환불 실패");
    
    private final String description;
    
    RefundStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
