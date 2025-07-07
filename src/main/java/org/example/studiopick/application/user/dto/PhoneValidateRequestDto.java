package org.example.studiopick.application.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneValidateRequestDto {

    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "휴대폰번호는 11자리 숫자여야 합니다.")
    private String phone;
}
