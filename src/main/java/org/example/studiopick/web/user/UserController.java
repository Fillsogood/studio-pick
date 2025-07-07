package org.example.studiopick.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.application.user.dto.EmailValidateRequestDto;
import org.example.studiopick.application.user.dto.UserSignupRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserSignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }
    // 이메일 중복검사 API
    @PostMapping("/validate/email")
    public ResponseEntity<String> validateEmail(@RequestBody @Valid EmailValidateRequestDto dto) {
        boolean available = userService.validateEmail(dto.getEmail());

        if (!available) {
            return ResponseEntity.badRequest().body("이미 사용 중인 이메일입니다.");
        }

        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }
}
