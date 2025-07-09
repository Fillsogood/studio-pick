package org.example.studiopick.web.user;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.application.user.dto.JwtTokenResponseDto;
import org.example.studiopick.application.user.dto.UserLoginRequestDto;
import org.example.studiopick.common.util.JwtUtil;
import org.example.studiopick.infrastructure.oauth.KakaoOAuthClient;
import org.example.studiopick.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDto> login(@RequestBody UserLoginRequestDto requestDto) {
        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        // 2. 인증 성공 → JWT 발급
        String accessToken = jwtProvider.createAccessToken(requestDto.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(requestDto.getEmail());

        // 3. 응답 반환
        return ResponseEntity.ok(new JwtTokenResponseDto(accessToken, refreshToken));
    }


    @PostMapping("/oauth/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String accessToken = kakaoOAuthClient.getAccessToken(code); // 여기서 호출됨

        // 이후 사용자 정보 요청, 로그인 처리 등 구현 예정
        return ResponseEntity.ok("AccessToken: " + accessToken);
    }

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
