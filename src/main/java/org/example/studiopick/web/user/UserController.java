package org.example.studiopick.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.dto.UserProfileResponseDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateRequestDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateResponseDto;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getUserId();
        UserProfileResponseDto profile = userService.getUserProfile(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", profile);

        return ResponseEntity.ok(response);
    }

    // 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        UserProfileUpdateResponseDto updated = userService.updateUserProfile(userId, requestDto);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필이 수정되었습니다",
                "data", updated
        ));
    }
}
