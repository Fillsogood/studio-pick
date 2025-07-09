package org.example.studiopick.web.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.dto.ChangePasswordRequestDto;
import org.example.studiopick.application.user.dto.UserProfileResponseDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateRequestDto;
import org.example.studiopick.application.user.dto.UserProfileUpdateResponseDto;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        userService.changePassword(userId, requestDto.getCurrentPassword(), requestDto.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "비밀번호가 변경되었습니다."
        ));
    }

    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("image") MultipartFile image
    ) {
        String imageUrl = userService.uploadProfileImage(userPrincipal.getUserId(), image);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "프로필 이미지가 업로드되었습니다",
                "data", Map.of("imageUrl", imageUrl)
        ));
    }

}
