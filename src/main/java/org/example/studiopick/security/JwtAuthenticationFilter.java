package org.example.studiopick.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {

            // Step 1: 블랙리스트 검사 먼저
            if (tokenService.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("로그아웃된 토큰입니다.");
                return;
            }

            // Step 2: 토큰 유효성 검사 및 인증 처리
            if (jwtProvider.validateToken(token) && jwtProvider.isAccessToken(token)) {
                String email = jwtProvider.getEmailFromToken(token);
                Long userId = jwtProvider.getUserIdFromToken(token);

                // DB 조회 없이 토큰 정보만으로 UserPrincipal 생성
                UserPrincipal userPrincipal = UserPrincipal.createFromToken(userId, email);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                Collections.singleton(() -> "ROLE_USER") // 기본 권한, 필요시 토큰에 role도 추가 가능
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    // Authorization Header와 Cookie에서 토큰 추출
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. Authorization Header에서 토큰 추출 시도
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2. 쿠키에서 토큰 추출 시도
        return getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
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
}
