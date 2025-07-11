package org.example.studiopick.application.studio.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record StudioCreateRequest(
    Long studioId,
    @NotBlank String description,
    @NotBlank String phone,
    @NotNull Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Integer maxPeople,
    Long perPersonRate,
    Long thumbnailId,
    List<OperatingHoursDto> operatingHours
) {}
