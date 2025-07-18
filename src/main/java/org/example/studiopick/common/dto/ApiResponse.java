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
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
