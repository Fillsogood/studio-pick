package org.example.studiopick.application.admin.dto.setting;

import java.time.LocalDateTime;

public record SystemSettingResponse(
    String settingKey,
    String settingValue,
    String description,
    String category,
    String dataType,
    boolean isEditable,
    String defaultValue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}