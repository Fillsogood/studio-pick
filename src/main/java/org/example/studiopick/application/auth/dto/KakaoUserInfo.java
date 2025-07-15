package org.example.studiopick.application.auth.dto;

import lombok.Getter;

/**
 * 카카오 사용자 정보를 담는 DTO
 */
@Getter
public class KakaoUserInfo {
    private final Long kakaoId;
    private final String email;
    private final String nickname;
    private final String profileImage;
    
    public KakaoUserInfo(Long kakaoId, String email, String nickname, String profileImage) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
