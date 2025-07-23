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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody @Valid UserSignupRequestDto requestDto) {
        authService.signup(requestDto);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 완료되었습니다"));
    }

    @Operation(summary = "이메일 중복 검사")
    @PostMapping("/validate/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestBody @Valid EmailValidateRequestDto dto) {
        boolean available = authService.validateEmail(dto.getEmail());
        if (!available) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 사용 중인 이메일입니다"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "사용 가능한 이메일입니다"));
    }

    @Operation(summary = "휴대폰 번호 중복 검사")
    @PostMapping("/validate/phone")
    public ResponseEntity<Map<String, Object>> validatePhone(@Valid @RequestBody PhoneValidateRequestDto requestDto) {
        boolean available = authService.validatePhone(requestDto.getPhone());
        if (!available) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "이미 사용 중인 휴대폰번호입니다"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "사용 가능한 휴대폰번호입니다"));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @RequestBody @Valid UserLoginRequestDto requestDto,
        HttpServletResponse response) {

        JwtTokenResponseDto tokenResponse = authService.login(requestDto);
        boolean rememberMe = requestDto.isRememberMe();
        int refreshMaxAge = rememberMe ? (60 * 60 * 24 * 14) : -1;

        ResponseCookie accessToken = createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, tokenResponse.getAccessToken(), (int) (accessTokenExpiration / 1000));
        ResponseCookie refreshToken = createResponseCookie(REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken(), refreshMaxAge);

        response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());

        return ResponseEntity.ok(Map.of("success", true, "message", "로그인되었습니다"));
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
        HttpServletRequest request,
        HttpServletResponse response) {

        String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Refresh Token이 필요합니다"));
        }

        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken);
            ResponseCookie accessTokenCookie = createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, newAccessToken, (int) (accessTokenExpiration / 1000));
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

            return ResponseEntity.ok(Map.of("success", true, "message", "Access Token이 재발급되었습니다"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/oauth/kakao")
    public ResponseEntity<Map<String, Object>> kakaoLogin(
        @RequestBody KakaoLoginRequestDto requestDto,
        HttpServletResponse response) {

        try {
            JwtTokenResponseDto tokenResponse = authService.kakaoLogin(requestDto.getCode());
            ResponseCookie accessToken = createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, tokenResponse.getAccessToken(), (int) (accessTokenExpiration / 1000));
            ResponseCookie refreshToken = createResponseCookie(REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken(), (int) (refreshTokenExpiration / 1000));

            response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshToken.toString());

            return ResponseEntity.ok(Map.of("success", true, "message", "카카오 로그인 성공"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "카카오 로그인 실패: " + e.getMessage()));
        }
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {

        String accessToken = getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = getTokenFromCookie(request, REFRESH_TOKEN_COOKIE_NAME);

        if (accessToken == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "로그인되지 않은 상태입니다"));
        }

        authService.logout(accessToken, refreshToken);

        ResponseCookie accessTokenCookie = createResponseCookie(ACCESS_TOKEN_COOKIE_NAME, "", 0);
        ResponseCookie refreshTokenCookie = createResponseCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃되었습니다"));
    }

    // === 헬퍼 ===

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

    private ResponseCookie createResponseCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(maxAge)
            .build();
    }
}
