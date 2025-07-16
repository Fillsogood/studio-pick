package org.example.studiopick.application.workshop.dto;

import java.util.List;

public record WorkShopListResponse(
    List<WorkShopListDto> classes
) {}
