package org.example.studiopick.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.domain.user.UserService;
import org.example.studiopick.domain.user.UserSignupRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
