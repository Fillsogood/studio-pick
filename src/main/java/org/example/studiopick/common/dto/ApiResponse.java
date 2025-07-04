package org.example.studiopick.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답")
public record ApiResponse<T>(
    @Schema(description = "성공 여부")
    boolean success,

    @Schema(description = "응답 데이터")
    T data,

    @Schema(description = "메시지")
    String message
) {}
