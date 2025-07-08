package org.example.studiopick.application.user.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenResponseDto {
    private String accessToken;
    private String refreshToken;
}