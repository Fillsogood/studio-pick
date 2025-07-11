package org.example.studiopick.application.studio.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudioUpdateRequest(
    String description,
    String phone,
    Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Integer maxPeople,
    Long perPersonRate,
    List<OperatingHoursDto> operatingHours,
    List<StudioImageUpdateDto> images // 이미지 순서 및 대표 여부 수정
) {}
