package org.example.studiopick.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.application.user.dto.*;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.util.JwtUtil;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.infrastructure.oauth.KakaoOAuthClient;
import org.example.studiopick.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // 쿠키 설정 상수
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 새 계정을 생성합니다")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    // 이메일 중복 검사
    @PostMapping("/validate/email")
    public ResponseEntity<String> validateEmail(@RequestBody @Valid EmailValidateRequestDto dto) {
        boolean available = userService.validateEmail(dto.getEmail());

        if (!available) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    // 휴대폰 중복 검사
    @PostMapping("/validate/phone")
    public ResponseEntity<String> validatePhone(@Valid @RequestBody PhoneValidateRequestDto requestDto) {
        boolean available = userService.validatePhone(requestDto.getPhone());

        if (!available) {
            return ResponseEntity.badRequest().body("이미 사용 중인 휴대폰번호입니다.");
        }

        return ResponseEntity.ok("사용 가능한 휴대폰번호입니다.");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 쿠키에 저장합니다")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody UserLoginRequestDto requestDto, 
            HttpServletResponse response) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        // 사용자 정보 조회 (ID 가져오기 위함)
        User user = userService.findByEmail(requestDto.getEmail());
        
        // 토큰 생성 시 사용자 ID도 포함
        String accessToken = jwtProvider.createAccessToken(requestDto.getEmail(), user.getId());
        String refreshToken = jwtProvider.createRefreshToken(requestDto.getEmail(), user.getId());

        // Access Token 쿠키 설정 (HttpOnly, Secure)
        Cookie accessTokenCookie = createTokenCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken, (int) (accessTokenExpiration / 1000));
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 설정 (HttpOnly, Secure)
        Cookie refreshTokenCookie = createTokenCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, (int) (refreshTokenExpiration / 1000));
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인되었습니다",
                "data", Map.of(
                        "email", user.getEmail(),
                        "userId", user.getId()
                )
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

        // 1. Refresh Token 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 Refresh Token입니다"
            ));
        }

        // 2. Refresh Token인지 확인
        if (!jwtProvider.isRefreshToken(refreshToken)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Refresh Token이 아닙니다"
            ));
        }

        // 3. 토큰이 블랙리스트에 있는지 확인
        if (tokenService.isBlacklisted(refreshToken)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "로그아웃된 토큰입니다"
            ));
        }

        // 4. 토큰에서 사용자 정보 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // 5. 새로운 Access Token만 생성
        String newAccessToken = jwtProvider.createAccessToken(email, userId);

        // 새로운 Access Token을 쿠키에 저장
        Cookie accessTokenCookie = createTokenCookie(ACCESS_TOKEN_COOKIE_NAME, newAccessToken, (int) (accessTokenExpiration / 1000));
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Access Token이 재발급되었습니다"
        ));
    }

    // 카카오 로그인
    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String accessToken = kakaoOAuthClient.getAccessToken(code);
        return ResponseEntity.ok("AccessToken: " + accessToken);
    }

    // 로그아웃 (쿠키의 토큰들을 블랙리스트에 등록하고 쿠키 삭제)
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

        // Access Token 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            long accessTokenExpiration = jwtUtil.getExpiration(accessToken);
            tokenService.blacklistToken(accessToken, accessTokenExpiration);
        }

        // Refresh Token 블랙리스트 처리
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            long refreshTokenExpiration = jwtUtil.getExpiration(refreshToken);
            tokenService.blacklistToken(refreshToken, refreshTokenExpiration);
        }

        // 쿠키 삭제
        clearTokenCookies(response);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃되었습니다"
        ));
    }

    // 쿠키 생성 헬퍼 메서드
    private Cookie createTokenCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // XSS 방지
        cookie.setSecure(false);   // 개발 환경에서는 false, 운영 환경에서는 true
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초 단위)
        return cookie;
    }

    // 쿠키에서 토큰 추출 헬퍼 메서드
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

    // 토큰 쿠키 삭제 헬퍼 메서드
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
