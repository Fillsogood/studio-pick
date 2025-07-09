package org.example.studiopick.web.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminStudioService;
import org.example.studiopick.application.admin.dto.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/studios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudioController {

  private final AdminStudioService adminStudioService;

  /**
   * 스튜디오 계정 목록 조회
   * GET /api/admin/studios?page=1&size=10&status=active&keyword=스튜디오명
   */
  @GetMapping
  public ResponseEntity<ApiResponse<AdminStudioListResponse>> getStudioAccounts(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String keyword
  ) {
    log.info("스튜디오 계정 목록 조회 요청: page={}, size={}, status={}, keyword={}",
        page, size, status, keyword);

    AdminStudioListResponse response = adminStudioService.getStudioAccounts(page, size, status, keyword);

    ApiResponse<AdminStudioListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 계정 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오 계정 상세 조회
   * GET /api/admin/studios/{studioId}
   */
  @GetMapping("/{studioId}")
  public ResponseEntity<ApiResponse<AdminStudioDetailResponse>> getStudioAccount(
      @PathVariable Long studioId
  ) {
    log.info("스튜디오 계정 상세 조회 요청: studioId={}", studioId);

    AdminStudioDetailResponse response = adminStudioService.getStudioAccount(studioId);

    ApiResponse<AdminStudioDetailResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 계정 상세 정보를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오 계정 생성
   * POST /api/admin/studios
   */
  @PostMapping
  public ResponseEntity<ApiResponse<AdminStudioCreateResponse>> createStudioAccount(
      @Valid @RequestBody AdminStudioCreateCommand command
  ) {
    log.info("스튜디오 계정 생성 요청: email={}, studioName={}",
        command.email(), command.studioName());

    AdminStudioCreateResponse response = adminStudioService.createStudioAccount(command);

    ApiResponse<AdminStudioCreateResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 계정이 생성되었습니다."
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  /**
   * 스튜디오 계정 수정
   * PUT /api/admin/studios/{studioId}
   */
  @PutMapping("/{studioId}")
  public ResponseEntity<ApiResponse<AdminStudioUpdateResponse>> updateStudioAccount(
      @PathVariable Long studioId,
      @Valid @RequestBody AdminStudioUpdateCommand command
  ) {
    log.info("스튜디오 계정 수정 요청: studioId={}", studioId);

    AdminStudioUpdateResponse response = adminStudioService.updateStudioAccount(studioId, command);

    ApiResponse<AdminStudioUpdateResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 계정이 수정되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오 계정 상태 변경 (승인/거부/정지/활성화)
   * PATCH /api/admin/studios/{studioId}/status
   */
  @PatchMapping("/{studioId}/status")
  public ResponseEntity<ApiResponse<AdminStudioStatusResponse>> changeStudioStatus(
      @PathVariable Long studioId,
      @Valid @RequestBody AdminStudioStatusCommand command
  ) {
    log.info("스튜디오 상태 변경 요청: studioId={}, status={}, reason={}",
        studioId, command.status(), command.reason());

    AdminStudioStatusResponse response = adminStudioService.changeStudioStatus(studioId, command);

    ApiResponse<AdminStudioStatusResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 상태가 변경되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오 계정 삭제 (소프트 삭제)
   * DELETE /api/admin/studios/{studioId}
   */
  @DeleteMapping("/{studioId}")
  public ResponseEntity<ApiResponse<Void>> deleteStudioAccount(
      @PathVariable Long studioId,
      @RequestParam(required = false, defaultValue = "관리자 요청") String reason
  ) {
    log.info("스튜디오 계정 삭제 요청: studioId={}, reason={}", studioId, reason);

    adminStudioService.deleteStudioAccount(studioId, reason);

    ApiResponse<Void> apiResponse = new ApiResponse<>(
        true,
        null,
        "스튜디오 계정이 삭제되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 스튜디오 통계 조회
   * GET /api/admin/studios/stats
   */
  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<AdminStudioStatsResponse>> getStudioStats() {
    log.info("스튜디오 통계 조회 요청");

    AdminStudioStatsResponse response = adminStudioService.getStudioStats();

    ApiResponse<AdminStudioStatsResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "스튜디오 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }
}