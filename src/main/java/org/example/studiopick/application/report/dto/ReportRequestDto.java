package org.example.studiopick.application.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.studiopick.domain.common.enums.ReportType;

public record ReportRequestDto (
        @NotNull(message = "신고 유형은 필수입니다.")
        ReportType reportedType,

        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long reportedId,

        @NotBlank(message = "신고 사유를 입력해주세요.")
        String reason

) {}
