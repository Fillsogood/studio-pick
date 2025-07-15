package org.example.studiopick.common.dto;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private String provider; // 소셜 로그인 에러용
    private String path; // 요청 경로
}
