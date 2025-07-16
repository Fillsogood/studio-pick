package org.example.studiopick.application.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final RedisTemplate<String, String> redisTemplate;

  public void blacklistToken(String accessToken, long expirationInMillis) {
    redisTemplate.opsForValue().set(
        accessToken,
        "logout",
        Duration.ofMillis(expirationInMillis)
    );
  }

  public boolean isBlacklisted(String accessToken) {
    return redisTemplate.hasKey(accessToken);
  }
}
