package org.example.studiopick.application.studio.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class StudioClassRequestDto {

    private Long studioId;          // 어떤 스튜디오의 클래스인지
    private String title;           // 클래스명
    private String description;     // 설명
    private BigDecimal price;       // 가격
    private LocalDate date;         // 날짜
    private LocalTime startTime;    // 시작 시간
    private LocalTime endTime;      // 종료 시간
    private String instructor;      // 강사명
    private String status;          // 상태 (OPEN, CLOSED)
}

