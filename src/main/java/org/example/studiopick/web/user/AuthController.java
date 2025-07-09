package org.example.studiopick.web.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.application.user.dto.*;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.util.JwtUtil;
import org.example.studiopick.infrastructure.oauth.KakaoOAuthClient;
import org.example.studiopick.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    // 회원가입
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

    // 이메일 로그인
    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDto> login(@RequestBody UserLoginRequestDto requestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        String accessToken = jwtProvider.createAccessToken(requestDto.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(requestDto.getEmail());

        return ResponseEntity.ok(new JwtTokenResponseDto(accessToken, refreshToken));
    }

    // 카카오 로그인
    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String accessToken = kakaoOAuthClient.getAccessToken(code);
        return ResponseEntity.ok("AccessToken: " + accessToken);
    }

    // 로그아웃 (토큰 블랙리스트 등록)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 요청입니다"
            ));
        }

        String accessToken = authHeader.substring(7);
        long expiration = jwtUtil.getExpiration(accessToken);
        tokenService.blacklistToken(accessToken, expiration);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그아웃되었습니다"
        ));
    }
}
