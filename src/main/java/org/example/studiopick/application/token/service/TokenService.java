package org.example.studiopick.application.token.service;

public interface TokenService {

    void blacklistToken(String accessToken, long expirationInMillis);

    boolean isBlacklisted(String accessToken);
}
