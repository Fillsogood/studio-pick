package org.example.studiopick.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminUserService;
import org.example.studiopick.application.admin.dto.user.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  /**
   * 사용자 계정 목록 조회
   * GET /api/admin/users?page=1&size=10&role=user&status=active&keyword=이름
   */
  @GetMapping
  public ResponseEntity<ApiResponse<AdminUserListResponse>> getUserAccounts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword
  ) {
    log.info("사용자 계정 목록 조회 요청: page={}, size={}, role={}, status={}, keyword={}",
        page, size, role, status, keyword);

    AdminUserListResponse response = adminUserService.getUserAccounts(page, size, role, status, keyword);

    ApiResponse<AdminUserListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 계정 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 계정 상세 조회
   * GET /api/admin/users/{userId}
   */
  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserAccount(
      @PathVariable Long userId
  ) {
    log.info("사용자 계정 상세 조회 요청: userId={}", userId);

    AdminUserDetailResponse response = adminUserService.getUserAccount(userId);

    ApiResponse<AdminUserDetailResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 계정 상세 정보를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 계정 생성
   * POST /api/admin/users
   */
  @PostMapping
  public ResponseEntity<ApiResponse<AdminUserCreateResponse>> createUserAccount(
      @Valid @RequestBody AdminUserCreateCommand command
  ) {
    log.info("사용자 계정 생성 요청: email={}, name={}, role={}",
        command.email(), command.name(), command.role());

    AdminUserCreateResponse response = adminUserService.createUserAccount(command);

    ApiResponse<AdminUserCreateResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 계정이 생성되었습니다."
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  /**
   * 사용자 계정 수정
   * PUT /api/admin/users/{userId}
   */
  @PutMapping("/{userId}")
  public ResponseEntity<ApiResponse<AdminUserUpdateResponse>> updateUserAccount(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserUpdateCommand command
  ) {
    log.info("사용자 계정 수정 요청: userId={}", userId);

    AdminUserUpdateResponse response = adminUserService.updateUserAccount(userId, command);

    ApiResponse<AdminUserUpdateResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 계정이 수정되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 계정 상태 변경 (활성화/비활성화/잠금)
   * PATCH /api/admin/users/{userId}/status
   */
  @PatchMapping("/{userId}/status")
  public ResponseEntity<ApiResponse<AdminUserStatusResponse>> changeUserStatus(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserStatusCommand command
  ) {
    log.info("사용자 상태 변경 요청: userId={}, status={}, reason={}",
        userId, command.status(), command.reason());

    AdminUserStatusResponse response = adminUserService.changeUserStatus(userId, command);

    ApiResponse<AdminUserStatusResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 상태가 변경되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 역할 변경 (USER ↔ STUDIO_OWNER)
   * PATCH /api/admin/users/{userId}/role
   */
  @PatchMapping("/{userId}/role")
  public ResponseEntity<ApiResponse<AdminUserRoleResponse>> changeUserRole(
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserRoleCommand command
  ) {
    log.info("사용자 역할 변경 요청: userId={}, role={}, reason={}",
        userId, command.role(), command.reason());

    AdminUserRoleResponse response = adminUserService.changeUserRole(userId, command);

    ApiResponse<AdminUserRoleResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 역할이 변경되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 계정 삭제 (소프트 삭제)
   * DELETE /api/admin/users/{userId}
   */
  @DeleteMapping("/{userId}")
  public ResponseEntity<ApiResponse<Void>> deleteUserAccount(
      @PathVariable Long userId,
      @RequestParam(required = false, defaultValue = "관리자 요청") String reason
  ) {
    log.info("사용자 계정 삭제 요청: userId={}, reason={}", userId, reason);

    adminUserService.deleteUserAccount(userId, reason);

    ApiResponse<Void> apiResponse = new ApiResponse<>(
        true,
        null,
        "사용자 계정이 삭제되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 사용자 통계 조회
   * GET /api/admin/users/stats
   */
  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<AdminUserStatsResponse>> getUserStats() {
    log.info("사용자 통계 조회 요청");

    AdminUserStatsResponse response = adminUserService.getUserStats();

    ApiResponse<AdminUserStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "사용자 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }
}
