package org.example.studiopick.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "카카오 로그인 요청 DTO")
@Getter
@NoArgsConstructor
public class KakaoLoginRequestDto {
    
    @Schema(description = "카카오에서 받은 인증 코드", example = "abc123def456")
    @NotBlank(message = "카카오 인증 코드는 필수입니다")
    private String code;
    
    @Schema(description = "카카오 로그인 후 리다이렉트될 URL", example = "http://localhost:3000/oauth/kakao/callback")
    private String redirectUri;
}
