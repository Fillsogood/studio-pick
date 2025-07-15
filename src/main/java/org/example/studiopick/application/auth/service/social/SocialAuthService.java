package org.example.studiopick.application.auth.service.social;

import org.example.studiopick.application.auth.dto.JwtTokenResponseDto;

/**
 * 소셜 로그인 처리를 위한 서비스 인터페이스
 */
public interface SocialAuthService {
    
    /**
     * 카카오 OAuth 로그인 처리 (완전한 플로우)
     * @param code 카카오에서 받은 인증 코드
     * @return JWT 토큰 응답
     */
    JwtTokenResponseDto processKakaoLogin(String code);
}
