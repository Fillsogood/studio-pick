package org.example.studiopick.application.auth.service.social;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.auth.dto.JwtTokenResponseDto;
import org.example.studiopick.application.auth.dto.KakaoUserInfo;
import org.example.studiopick.common.exception.DuplicateResourceException;
import org.example.studiopick.common.exception.social.SocialLoginException;
import org.example.studiopick.domain.common.enums.SocialProvider;
import org.example.studiopick.domain.common.enums.UserRole;
import org.example.studiopick.domain.common.enums.UserStatus;
import org.example.studiopick.domain.user.entity.SocialAccount;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.domain.user.repository.SocialAccountRepository;
import org.example.studiopick.domain.user.repository.UserRepository;
import org.example.studiopick.infrastructure.oauth.KakaoOAuthClient;
import org.example.studiopick.security.JwtProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * 소셜 로그인 처리를 위한 서비스 구현체 (Auth 중심 완전 통합)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialAuthServiceImpl implements SocialAuthService {
    
    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;
    
    /**
     * 카카오 OAuth 로그인 처리 (완전한 플로우 구현)
     */
    @Override
    @Transactional
    public JwtTokenResponseDto processKakaoLogin(String code) {
        log.info("카카오 로그인 처리 시작: code={}", code);
        
        try {
            // 1. 카카오 액세스 토큰 획득
            String kakaoAccessToken = kakaoOAuthClient.getAccessToken(code);
            log.debug("카카오 액세스 토큰 획득 완료");
            
            // 2. 카카오 사용자 정보 조회
            Map<String, Object> userInfoResponse = kakaoOAuthClient.getUserInfo(kakaoAccessToken);
            log.debug("카카오 사용자 정보 조회 완료");
            
            // 3. 카카오 사용자 정보 파싱
            KakaoUserInfo kakaoUserInfo = parseKakaoUserInfo(userInfoResponse);
            log.info("카카오 사용자 정보 파싱 완료: kakaoId={}, email={}", kakaoUserInfo.getKakaoId(), kakaoUserInfo.getEmail());
            
            // 4. 소셜 사용자 처리 (로그인/회원가입)
            String socialId = "kakao_" + kakaoUserInfo.getKakaoId();
            User user = loginOrRegisterSocialUser(
                    "KAKAO",
                    socialId,
                    kakaoUserInfo.getEmail(),
                    kakaoUserInfo.getNickname(),
                    kakaoUserInfo.getProfileImage()
            );
            
            // 5. JWT 토큰 생성
            String accessToken = jwtProvider.createAccessToken(user.getEmail(), user.getId(), user.getRole());
            String refreshToken = jwtProvider.createRefreshToken(user.getEmail(), user.getId(), user.getRole());
            
            log.info("카카오 로그인 성공: userId={}, email={}", user.getId(), user.getEmail());
            
            return new JwtTokenResponseDto(accessToken, refreshToken);
            
        } catch (SocialLoginException e) {
            log.error("카카오 로그인 실패 - SocialLoginException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 예상치 못한 오류 발생", e);
            throw new SocialLoginException("KAKAO", "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 소셜 로그인 사용자 처리 (Auth 서비스 내부 로직)
     */
    private User loginOrRegisterSocialUser(String provider, String socialId, String email, String nickname, String profileImage) {
        log.info("소셜 사용자 처리: provider={}, socialId={}", provider, socialId);
        
        try {
            SocialProvider providerEnum = SocialProvider.valueOf(provider.toUpperCase());
            
            // 기존 소셜 계정 조회
            Optional<User> existingUser = userRepository
                    .findBySocialAccountsProviderAndSocialAccountsSocialId(providerEnum, socialId);
            
            if (existingUser.isPresent()) {
                log.info("기존 소셜 사용자 로그인: userId={}", existingUser.get().getId());
                return existingUser.get();
            }
            
            // 신규 사용자 생성
            return createSocialUser(providerEnum, socialId, email, nickname, profileImage);
            
        } catch (IllegalArgumentException e) {
            throw new DuplicateResourceException("지원하지 않는 소셜 로그인 제공자: " + provider);
        }
    }
    
    /**
     * 소셜 사용자 생성
     */
    private User createSocialUser(SocialProvider provider, String socialId, String email, String nickname, String profileImage) {
        User newUser = User.builder()
                .email(email != null ? email : generateSocialEmail(provider.getValue(), socialId))
                .password(null) // 소셜 로그인은 비밀번호 없음
                .name(nickname != null ? nickname : "소셜회원")
                .nickname(generateUniqueNickname(nickname != null ? nickname : "소셜회원"))
                .profileImageUrl(profileImage)
                .isStudioOwner(false)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();
        
        userRepository.save(newUser);
        
        // 소셜 계정 정보 저장
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(provider)
                .socialId(socialId)
                .user(newUser)
                .build();
        
        socialAccountRepository.save(socialAccount);
        
        log.info("신규 소셜 사용자 생성: userId={}, provider={}", newUser.getId(), provider.getValue());
        return newUser;
    }
    
    /**
     * 중복되지 않는 닉네임 생성
     */
    private String generateUniqueNickname(String baseName) {
        String baseNickname = baseName != null ? baseName : "소셜사용자";
        String nickname = baseNickname;
        int suffix = 1;
        
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + suffix++;
        }
        
        return nickname;
    }
    
    /**
     * 소셜 로그인용 이메일 생성
     */
    private String generateSocialEmail(String provider, String socialId) {
        return provider.toLowerCase() + "_" + socialId + "@social.studiopick.com";
    }
    
    /**
     * 카카오 사용자 정보 파싱
     */
    private KakaoUserInfo parseKakaoUserInfo(Map<String, Object> userInfo) {
        try {
            Long kakaoId = ((Number) userInfo.get("id")).longValue();
            
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
            if (kakaoAccount == null) {
                throw new SocialLoginException("KAKAO", "카카오 계정 정보를 찾을 수 없습니다");
            }
            
            String email = (String) kakaoAccount.get("email");
            
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile == null) {
                throw new SocialLoginException("KAKAO", "카카오 프로필 정보를 찾을 수 없습니다");
            }
            
            String nickname = (String) profile.get("nickname");
            String profileImage = (String) profile.get("profile_image_url");
            
            return new KakaoUserInfo(kakaoId, email, nickname, profileImage);
            
        } catch (ClassCastException | NullPointerException e) {
            log.error("카카오 사용자 정보 파싱 실패: {}", userInfo, e);
            throw new SocialLoginException("KAKAO", "카카오 사용자 정보 형식이 올바르지 않습니다");
        }
    }
}
