package org.example.studiopick.application.studio.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponseDto {

    private long todayReservationCount;      // 오늘 예약 수
    private long monthReservationCount;      // 이번 달 예약 수
    private long todayRevenue;               // 오늘 매출
    private long monthRevenue;               // 이번 달 매출
    private long newCustomerCount;           // 신규 고객 수
    private long classCount;                 // 클래스 수
}
