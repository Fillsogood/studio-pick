package org.example.studiopick.domain.common.enums;

import lombok.Getter;

@Getter
public enum SocialProvider {
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    private final String value;

    SocialProvider(String value) {
        this.value = value;
    }

}
