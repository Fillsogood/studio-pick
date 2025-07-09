package org.example.studiopick.application.admin.dto.setting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SystemSettingCreateCommand(
    @NotBlank(message = "설정 키는 필수입니다")
    @Size(min = 3, max = 100, message = "설정 키는 3-100자 사이여야 합니다")
    @Pattern(regexp = "^[a-z0-9._-]+$", message = "설정 키는 소문자, 숫자, '.', '_', '-'만 사용 가능합니다")
    String settingKey,

    @NotBlank(message = "설정값은 필수입니다")
    @Size(max = 500, message = "설정값은 500자 이하여야 합니다")
    String settingValue,

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    String description,

    @NotBlank(message = "카테고리는 필수입니다")
    @Pattern(regexp = "^(BUSINESS|SYSTEM|NOTIFICATION|PAYMENT|RESERVATION)$",
        message = "유효하지 않은 카테고리입니다")
    String category,

    @NotBlank(message = "데이터 타입은 필수입니다")
    @Pattern(regexp = "^(STRING|INTEGER|DECIMAL|BOOLEAN)$",
        message = "유효하지 않은 데이터 타입입니다")
    String dataType,

    Boolean isEditable,

    @Size(max = 500, message = "기본값은 500자 이하여야 합니다")
    String defaultValue
) {}
