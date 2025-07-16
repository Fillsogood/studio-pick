package org.example.studiopick.web.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.auth.dto.*;
import org.example.studiopick.application.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;


import java.util.Arrays;
import java.util.Map;

@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 쿠키 설정 상수
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 새 계정을 생성합니다")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid UserSignupRequestDto requestDto) {
        authService.signup(requestDto);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다"
        ));
    }

    @Operation(summary = "이메일 중복 검사", description = "회원가입 시 이메일 중복을 검사합니다")
    @PostMapping("/validate/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestBody @Valid EmailValidateRequestDto dto) {
        boolean available = authService.validateEmail(dto.getEmail());

        if (!available) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미 사용 중인 이메일입니다"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용 가능한 이메일입니다"
        ));
    }

    @Operation(summary = "휴대폰 번호 중복 검사", description = "회원가입 시 휴대폰 번호 중복을 검사합니다")
    @PostMapping("/validate/phone")
    public ResponseEntity<Map<String, Object>> validatePhone(@Valid @RequestBody PhoneValidateRequestDto requestDto) {
        boolean available = authService.validatePhone(requestDto.getPhone());

        if (!available) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미 사용 중인 휴대폰번호입니다"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용 가능한 휴대폰번호입니다"
        ));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 쿠키에 저장합니다")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody @Valid UserLoginRequestDto requestDto, 
            HttpServletResponse response) {
        
        // AuthService를 통한 로그인 처리
        JwtTokenResponseDto tokenResponse = authService.login(requestDto);

        ResponseCookie accessToken = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, tokenResponse.getAccessToken())
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Lax")
            .maxAge(accessTokenExpiration / 1000)
            .build();

        ResponseCookie refreshToken = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken())
            .httpOnly(true)
            .secure(false)
            .path("/")
            .sameSite("Lax")
            .maxAge(refreshTokenExpiration / 1000)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인되었습니다"
        ));
    }

    @Operation(summary = "토큰 재발급", description = "쿠키의 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // 쿠키에서 Refresh Token 추출
        String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Refresh Token이 필요합니다"
            ));
        }

        try {
            // AuthService를 통한 토큰 재발급
            String newAccessToken = authService.refreshAccessToken(refreshToken);

            // 새로운 Access Token을 쿠키에 저장
            Cookie accessTokenCookie = createTokenCookie(
                    ACCESS_TOKEN_COOKIE_NAME, 
                    newAccessToken, 
                    (int) (accessTokenExpiration / 1000)
            );
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Access Token이 재발급되었습니다"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "카카오 로그인", description = "카카오 OAuth를 통한 로그인 (완전한 기능)")
    @PostMapping("/oauth/kakao")
    public ResponseEntity<Map<String, Object>> kakaoLogin(
            @RequestBody KakaoLoginRequestDto requestDto,
            HttpServletResponse response) {
        try {
            JwtTokenResponseDto tokenResponse = authService.kakaoLogin(requestDto.getCode());
            
            // JWT 토큰을 쿠키에 저장
            setTokenCookies(response, tokenResponse);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "카카오 로그인 성공"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "카카오 로그인 실패: " + e.getMessage()
            ));
        }
    }
    
    /**
     * JWT 토큰들을 쿠키에 저장하는 통합 메서드
     */
    private void setTokenCookies(HttpServletResponse response, JwtTokenResponseDto tokenResponse) {
        Cookie accessTokenCookie = createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME, 
                tokenResponse.getAccessToken(), 
                (int) (accessTokenExpiration / 1000)
        );
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME, 
                tokenResponse.getRefreshToken(), 
                (int) (refreshTokenExpiration / 1000)
        );
        response.addCookie(refreshTokenCookie);
    }

    @Operation(summary = "로그아웃", description = "쿠키의 토큰들을 무효화하고 쿠키를 삭제합니다")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // 쿠키에서 토큰들 추출
        String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "로그인되지 않은 상태입니다"
            ));
        }

        // AuthService를 통한 로그아웃 처리
        authService.logout(accessToken, refreshToken);

        // 쿠키 삭제
        clearTokenCookies(response);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃되었습니다"
        ));
    }

    // === 헬퍼 메서드들 ===
    
    /**
     * 쿠키 생성 헬퍼 메서드
     */
    private Cookie createTokenCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // XSS 방지
        cookie.setSecure(false);   // 개발 환경에서는 false, 운영 환경에서는 true
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초 단위)
        return cookie;
    }

    /**
     * 쿠키에서 토큰 추출 헬퍼 메서드
     */
    private String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }

    /**
     * 토큰 쿠키 삭제 헬퍼 메서드
     */
    private void clearTokenCookies(HttpServletResponse response) {
        // Access Token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // 개발 환경에서는 false
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 개발 환경에서는 false
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 즉시 만료
        response.addCookie(refreshTokenCookie);
    }
}
