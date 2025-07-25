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
    private String profileImageUrl;
    private boolean isStudioOwner;
    private boolean isWorkShopOwner;
    private String status;
    private LocalDateTime createdAt;
    private String role;
}
