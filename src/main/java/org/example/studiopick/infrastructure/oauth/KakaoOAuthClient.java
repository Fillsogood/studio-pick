package org.example.studiopick.infrastructure.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.common.exception.social.SocialLoginException;
import org.example.studiopick.common.enums.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(tokenUrl)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            log.info("카카오 토큰 요청 성공: status={}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            if (body == null || body.get("access_token") == null) {
                throw new SocialLoginException("KAKAO", ErrorCode.OAUTH_TOKEN_REQUEST_FAILED.getMessage());
            }

            return body.get("access_token").toString();

        } catch (ResourceAccessException e) {
            log.error("카카오 토큰 요청 네트워크 오류", e);
            throw new SocialLoginException("KAKAO", "카카오 서버 연결에 실패했습니다");
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new SocialLoginException("KAKAO", "카카오 액세스 토큰 요청 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.info("카카오 사용자 정보 조회 성공: status={}", response.getStatusCode());

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new SocialLoginException("KAKAO", "카카오 사용자 정보 조회 실패 (응답 없음)");
            }

            return body;

        } catch (ResourceAccessException e) {
            log.error("카카오 사용자 정보 요청 네트워크 오류", e);
            throw new SocialLoginException("KAKAO", "카카오 서버 연결에 실패했습니다");
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new SocialLoginException("KAKAO", "카카오 사용자 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
