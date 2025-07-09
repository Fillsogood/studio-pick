package org.example.studiopick.application.admin.dto.user;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;


public record AdminUserCreateCommand(
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 20, message = "이름은 2-20자 사이여야 합니다")
    String name,

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다")
    String password,

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^01[0-9]-[0-9]{3,4}-[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    String phone,

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 10, message = "닉네임은 2-10자 사이여야 합니다")
    String nickname,

    @NotBlank(message = "역할은 필수입니다")
    String role
) {}