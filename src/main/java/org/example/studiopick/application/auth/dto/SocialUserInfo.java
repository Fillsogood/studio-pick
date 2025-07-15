package org.example.studiopick.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 사용자 정보를 담는 통일된 DTO (카카오 전용)
 */
@Getter
@Builder
public class SocialUserInfo {
    
    /**
     * 소셜 제공자 (KAKAO)
     */
    private final String provider;
    
    /**
     * 스튜디오픽에서 사용할 소셜 ID (kakao_원본ID 형태)
     * 예: kakao_12345
     */
    private final String socialId;
    
    /**
     * 카카오 원본 사용자 ID
     */
    private final Long kakaoId;
    
    /**
     * 사용자 이메일
     */
    private final String email;
    
    /**
     * 사용자 닉네임
     */
    private final String nickname;
    
    /**
     * 프로필 이미지 URL
     */
    private final String profileImage;
    
    /**
     * 카카오 사용자용 생성자
     */
    public static SocialUserInfo createKakaoUser(Long kakaoId, String email, String nickname, String profileImage) {
        return SocialUserInfo.builder()
                .provider("KAKAO")
                .socialId("kakao_" + kakaoId)
                .kakaoId(kakaoId)
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
    
    /**
     * 이메일이 없는 경우 기본 이메일 생성
     */
    public String getEmailOrDefault() {
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return "kakao_" + kakaoId + "@social.studiopick.com";
    }
    
    /**
     * 닉네임이 없는 경우 기본 닉네임 생성
     */
    public String getNicknameOrDefault() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return "카카오사용자";
    }
    
    @Override
    public String toString() {
        return "SocialUserInfo{" +
                "provider='" + provider + '\'' +
                ", socialId='" + socialId + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
