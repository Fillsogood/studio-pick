package org.example.studiopick.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequestDto {

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 010으로 시작하고 총 11자리여야 합니다.")
    private String phone;

    @NotBlank
    private String nickname;
}
