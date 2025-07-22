package org.example.studiopick.application.workshop.dto;

/**
 * 호스트 센터 > 클래스 관리에서
 * 숨기기/다시 노출 기능을 위한 상태 변경 요청 DTO
 */
public record WorkshopStatusUpdateRequest(
        String status  // "ACTIVE" 또는 "INACTIVE"
) {}

