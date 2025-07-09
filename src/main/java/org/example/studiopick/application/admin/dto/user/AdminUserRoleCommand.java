package org.example.studiopick.application.admin.dto.user;

import jakarta.validation.constraints.*;

public record AdminUserRoleCommand(
    @NotBlank(message = "역할은 필수입니다")
    String role,

    @Size(max = 200, message = "사유는 200자 이하여야 합니다")
    String reason
) {}
