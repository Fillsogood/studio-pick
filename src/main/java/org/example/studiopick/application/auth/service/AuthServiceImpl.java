package org.example.studiopick.application.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.auth.dto.*;
import org.example.studiopick.application.auth.service.social.SocialAuthService;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.exception.DuplicateResourceException;
import org.example.studiopick.common.exception.auth.TokenException;
import org.example.studiopick.common.enums.ErrorCode;
import org.example.studiopick.common.util.JwtUtil;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.security.JwtProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final SocialAuthService socialAuthService;
    
    /**
     * 사용자 회원가입
     */
    @Override
    @Transactional
    public void signup(UserSignupRequestDto requestDto) {
        log.info("회원가입 요청: email={}, name={}", requestDto.getEmail(), requestDto.getName());
        
        // 비밀번호 확인 검증
        if (!requestDto.getPassword().equals(requestDto.getConfirmPassword())) {
            throw new DuplicateResourceException(ErrorCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
        }
        
        // 이메일 중복 검사
        if (!validateEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("이메일");
        }
        
        // 휴대폰 번호 중복 검사
        if (!validatePhone(requestDto.getPhone())) {
            throw new DuplicateResourceException("휴대폰 번호");
        }
        
        // UserService를 통해 사용자 생성
        userService.createUser(requestDto);
        
        log.info("회원가입 완료: email={}", requestDto.getEmail());
    }
    
    /**
     * 이메일 중복 검사
     */
    @Override
    public boolean validateEmail(String email) {
        return userService.validateEmail(email);
    }
    
    /**
     * 휴대폰 번호 중복 검사
     */
    @Override
    public boolean validatePhone(String phone) {
        return userService.validatePhone(phone);
    }
    
    /**
     * 로그인 처리 및 토큰 생성
     */
    @Override
    @Transactional
    public JwtTokenResponseDto login(UserLoginRequestDto requestDto) {
        log.info("로그인 요청: email={}", requestDto.getEmail());
        
        // Spring Security를 통한 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );
        
        // 사용자 정보 조회
        User user = userService.findByEmail(requestDto.getEmail());
        
        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(requestDto.getEmail(), user.getId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(requestDto.getEmail(), user.getId(), user.getRole());
        
        log.info("로그인 성공: email={}, userId={}", requestDto.getEmail(), user.getId());
        
        return new JwtTokenResponseDto(accessToken, refreshToken);
    }
    
    /**
     * 토큰 재발급
     */
    @Override
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        log.info("토큰 재발급 요청");
        
        // Refresh Token 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new TokenException(ErrorCode.INVALID_TOKEN, "유효하지 않은 Refresh Token입니다");
        }
        
        // Refresh Token인지 확인
        if (!jwtProvider.isRefreshToken(refreshToken)) {
            throw new TokenException(ErrorCode.INVALID_TOKEN, "Refresh Token이 아닙니다");
        }
        
        // 블랙리스트 확인
        if (tokenService.isBlacklisted(refreshToken)) {
            throw new TokenException(ErrorCode.TOKEN_EXPIRED, "로그아웃된 토큰입니다");
        }
        
        // 토큰에서 사용자 정보 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        UserRole role = jwtProvider.getRoleFromToken(refreshToken);
        
        // 새로운 Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(email, userId, role);
        
        log.info("토큰 재발급 성공: email={}, userId={}", email, userId);
        
        return newAccessToken;
    }
    
    /**
     * 로그아웃 처리 (토큰 무효화)
     */
    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        log.info("로그아웃 요청");
        
        // Access Token 블랙리스트 처리
        if (accessToken != null && jwtProvider.validateToken(accessToken)) {
            long accessTokenExpiration = jwtUtil.getExpiration(accessToken);
            tokenService.blacklistToken(accessToken, accessTokenExpiration);
        }
        
        // Refresh Token 블랙리스트 처리
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            long refreshTokenExpiration = jwtUtil.getExpiration(refreshToken);
            tokenService.blacklistToken(refreshToken, refreshTokenExpiration);
        }
        
        log.info("로그아웃 완료");
    }

    /**
     * 카카오 OAuth 로그인 (SocialAuthService로 위임)
     */
    @Override
    @Transactional
    public JwtTokenResponseDto kakaoLogin(String code) {
        return socialAuthService.processKakaoLogin(code);
    }
}
