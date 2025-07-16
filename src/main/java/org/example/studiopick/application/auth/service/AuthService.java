package org.example.studiopick.application.auth.service;

import org.example.studiopick.application.auth.dto.*;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface AuthService {
    
    /**
     * 사용자 회원가입
     */
    void signup(UserSignupRequestDto requestDto);
    
    /**
     * 이메일 중복 검사
     */
    boolean validateEmail(String email);
    
    /**
     * 휴대폰 번호 중복 검사
     */
    boolean validatePhone(String phone);
    
    /**
     * 로그인 처리 및 토큰 생성
     */
    JwtTokenResponseDto login(UserLoginRequestDto requestDto);
    
    /**
     * 토큰 재발급
     */
    String refreshAccessToken(String refreshToken);
    
    /**
     * 로그아웃 처리 (토큰 무효화)
     */
    void logout(String accessToken, String refreshToken);
    
    /**
     * 카카오 OAuth 로그인
     */
    JwtTokenResponseDto kakaoLogin(String code);
}
