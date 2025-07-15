package org.example.studiopick.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.domain.common.enums.UserRole;
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

            // [옵션] 블랙리스트 체크 (테스트 시 임시 주석 가능)
            if (tokenService.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("로그아웃된 토큰입니다.");
                return;
            }

            // 유효성 검사 및 인증 처리
            if (jwtProvider.validateToken(token)) { // isAccessToken 체크 생략 가능

                String email = jwtProvider.getEmailFromToken(token);
                Long userId = jwtProvider.getUserIdFromToken(token);
                UserRole role = jwtProvider.getRoleFromToken(token);

                UserPrincipal userPrincipal = UserPrincipal.createFromToken(userId, email, role);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        Collections.singleton(() -> "ROLE_" + role.name())  // ✅ ROLE_ 접두어 필요
                    );

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return getTokenFromCookie(request, ACCESS_TOKEN_COOKIE_NAME);
    }

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
