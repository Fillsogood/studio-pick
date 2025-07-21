package org.example.studiopick.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.token.service.TokenService;
import org.example.studiopick.security.JwtAuthenticationFilter;
import org.example.studiopick.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ 공개 API (비로그인 허용)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/validate/**",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/oauth/**",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/auth/oauth/**",
                                "/oauth2/**", // OAuth2 리디렉션 경로 허용
                                "/api/studios",    // 스튜디오 검색 공개
                                "/api/studios/search",    // 스튜디오 검색 공개
                                "/api/studios/{studioiId}",
                                "/api/studios/rental",
                                "/api/payments/request",
                                "/api/payments/confirm",
                                "/api/reservations/{reservationId}/cancel",
                                "/api/payments/{paymentKey}/cancel",
                                "/api/users/password/reset-request",
                                "/api/users/password/reset",

                                // ✅ 클래스 탐색은 GET만 허용
                                "/api/classes",
                                "/api/classes/{id}",
                                "/api/classes/images/**"
                        ).permitAll()

                        // ✅ 클래스 등록 및 이미지 업로드 등은 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/classes").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/classes/**").authenticated()

                        // ✅ 예약/결제 관련은 필요시 추가
                        .requestMatchers("/api/payments/request", "/api/payments/confirm").permitAll()

                        // ✅ 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ✅ 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ 401/403 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                        })
                )

                // ✅ JWT 인증 필터 등록
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, tokenService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
