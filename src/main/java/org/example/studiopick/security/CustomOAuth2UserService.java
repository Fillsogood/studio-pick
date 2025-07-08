package org.example.studiopick.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.domain.user.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // ex: kakao
        String accessToken = userRequest.getAccessToken().getTokenValue();

        log.info("OAuth2 로그인 요청 - provider: {}", registrationId);

        // 카카오인 경우 사용자 정보 파싱
        if ("kakao".equals(registrationId)) {
            return processKakaoUser(oAuth2User);
        }

        throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
    }

    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
        // 여기서 카카오 사용자 정보를 꺼내고, userService를 통해 처리하게 될 거야 (다음 단계에서 구현)

        log.info("카카오 사용자 정보: {}", oAuth2User.getAttributes());

        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long kakaoId = ((Number) attributes.get("id")).longValue();
        String socialId = "kakao_" + kakaoId;

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        // userService에 로그인/회원가입 처리 위임 (다음 단계에서 구현)
        User user = userService.loginOrRegisterSocialUser("kakao", socialId, email, nickname, profileImage);

        // UserPrincipal로 감싸서 반환
        return UserPrincipal.create(user, attributes);

    }
}
