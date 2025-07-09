package org.example.studiopick.application.admin.dto.setting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemSettingUpdateCommand(
    @NotBlank(message = "설정값은 필수입니다")
    @Size(max = 500, message = "설정값은 500자 이하여야 합니다")
    String settingValue,

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    String description
) {}