package org.example.studiopick.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.studiopick.domain.common.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKeyPlain;

    private SecretKey secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());
    }

    public String createAccessToken(String email, Long userId, UserRole role) {
        return createToken(email, userId, role,accessTokenExpiration, "ACCESS");
    }

    public String createRefreshToken(String email, Long userId, UserRole role) {
        return createToken(email, userId, role, refreshTokenExpiration, "REFRESH");
    }

    private String createToken(String email, Long userId, UserRole role, long expireTime, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireTime);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)  // 사용자 ID 추가
                .claim("role", role) //권환
                .claim("tokenType", tokenType)  // 토큰 타입 추가
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getTokenTypeFromToken(String token) {
        return getClaims(token).get("tokenType", String.class);
    }

    public UserRole getRoleFromToken(String token) {
        String role = getClaimsFromToken(token).get("role", String.class);
        if (role == null) throw new IllegalArgumentException("권한 정보가 없습니다");
        return UserRole.valueOf(role);
    }


    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)   // ✅ SecretKey 직접 전달
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "REFRESH".equals(getTokenTypeFromToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            return "ACCESS".equals(getTokenTypeFromToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
