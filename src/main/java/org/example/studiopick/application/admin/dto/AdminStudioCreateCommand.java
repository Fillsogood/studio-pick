package org.example.studiopick.application.admin.dto;

import jakarta.validation.constraints.*;

public record AdminStudioCreateCommand(
    @NotBlank(message = "스튜디오명은 필수입니다")
    @Size(min = 2, max = 50, message = "스튜디오명은 2-50자 사이여야 합니다")
    String studioName,

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    String description,

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    String address,

    @Pattern(regexp = "^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    String studioPhone,

    @NotNull(message = "시간당 기본요금은 필수입니다")
    @Min(value = 0, message = "시간당 기본요금은 0 이상이어야 합니다")
    Long hourlyBaseRate,

    @NotNull(message = "인당 추가요금은 필수입니다")
    @Min(value = 0, message = "인당 추가요금은 0 이상이어야 합니다")
    Long perPersonRate,

    @NotBlank(message = "소유자명은 필수입니다")
    @Size(min = 2, max = 20, message = "소유자명은 2-20자 사이여야 합니다")
    String ownerName,

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다")
    String password,

    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    String phone
) {}