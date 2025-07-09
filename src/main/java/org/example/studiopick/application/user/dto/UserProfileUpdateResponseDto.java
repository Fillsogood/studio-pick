package org.example.studiopick.application.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileUpdateResponseDto {

    private Long id;
    private String name;
    private String phone;
    private String nickname;
}
