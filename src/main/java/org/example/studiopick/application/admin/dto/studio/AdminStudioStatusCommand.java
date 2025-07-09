package org.example.studiopick.application.admin.dto.studio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminStudioStatusCommand(
    @NotBlank(message = "상태는 필수입니다")
    String status,

    @Size(max = 200, message = "사유는 200자 이하여야 합니다")
    String reason
) {}