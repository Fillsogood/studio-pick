package org.example.studiopick.web.user;


import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.dto.JwtTokenResponseDto;
import org.example.studiopick.application.user.dto.UserLoginRequestDto;
import org.example.studiopick.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

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
}
