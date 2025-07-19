package org.example.studiopick.application.studio.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record StudioCreateRequest(
    @NotBlank String name,
    @NotBlank String description,
    @NotBlank String phone,
    @NotNull Long hourlyBaseRate,
    BigDecimal weekendPrice,
    Integer maxPeople,
    Long perPersonRate,
    Integer size,
    String facilities,
    String rules,
    String thumbnailImage,
    List<String> imageUrls,
    List<OperatingHoursDto> operatingHours
) {}
