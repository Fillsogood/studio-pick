package org.example.studiopick.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenResponseDto {
    private String accessToken;
    private String refreshToken;
}
