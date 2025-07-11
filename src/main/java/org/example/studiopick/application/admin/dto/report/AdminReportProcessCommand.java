package org.example.studiopick.application.admin.dto.report;

import jakarta.validation.constraints.NotNull;
import org.example.studiopick.domain.common.enums.ReportStatus;

public record AdminReportProcessCommand(
        @NotNull(message = "처리 상태는 필수입니다.")
        ReportStatus status,
        
        String adminComment
) {}