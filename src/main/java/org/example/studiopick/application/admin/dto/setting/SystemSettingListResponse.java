package org.example.studiopick.application.admin.dto.setting;

import java.util.List;

public record SystemSettingListResponse(
    List<SystemSettingResponse> settings,
    String category,
    long totalCount
) {}