package org.example.studiopick.application.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PasswordResetConfirmDto {

    @NotBlank(message = "토큰은 필수입니다.")
    private String token;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
