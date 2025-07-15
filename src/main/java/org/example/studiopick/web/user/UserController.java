package org.example.studiopick.web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "사용자", description = "사용자 프로필 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // 토큰에서 직접 사용자 ID 추출 (DB 조회 없음)
        Long userId = userPrincipal.getUserId();
        UserProfileResponseDto profile = userService.getUserProfile(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
        ));
    }

    @Operation(summary = "프로필 수정", description = "사용자의 프로필 정보를 수정합니다")
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        try {
            // 토큰에서 직접 사용자 ID 추출
            Long userId = userPrincipal.getUserId();
            UserProfileUpdateResponseDto updated = userService.updateUserProfile(userId, requestDto);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로필이 수정되었습니다",
                    "data", updated
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다")
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequestDto requestDto
    ) {
        try {
            // 토큰에서 직접 사용자 ID 추출
            Long userId = userPrincipal.getUserId();
            userService.changePassword(userId, requestDto.getCurrentPassword(), requestDto.getNewPassword());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 변경되었습니다"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다")
    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("image") MultipartFile image
    ) {
        try {
            // 토큰에서 직접 사용자 ID 추출
            Long userId = userPrincipal.getUserId();
            String imageUrl = userService.uploadProfileImage(userId, image);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로필 이미지가 업로드되었습니다",
                    "data", Map.of("imageUrl", imageUrl)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "이미지 업로드에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "계정 비활성화", description = "사용자 계정을 비활성화합니다")
    @PatchMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        // TODO: 계정 비활성화 로직 구현
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "계정이 비활성화되었습니다"
        ));
    }

    @Operation(summary = "내 정보 확인", description = "현재 로그인한 사용자 정보를 반환합니다")
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
            "role", user.getRole()
        ));
    }
}
