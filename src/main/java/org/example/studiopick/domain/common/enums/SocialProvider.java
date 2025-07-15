package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum SocialProvider {
    KAKAO("kakao");
    // TODO: 향후 추가 예정
    // NAVER("naver"),
    // GOOGLE("google");

    private final String value;

    SocialProvider(String value) {
        this.value = value;
    }
}
