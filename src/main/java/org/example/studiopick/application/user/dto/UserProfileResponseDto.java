package org.example.studiopick.application.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponseDto {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String nickname;
    private boolean isStudioOwner;
    private String status;
    private LocalDateTime createdAt;
}
