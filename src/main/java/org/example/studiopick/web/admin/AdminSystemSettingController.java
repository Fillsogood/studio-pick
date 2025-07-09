package org.example.studiopick.web.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.admin.AdminSystemSettingService;
import org.example.studiopick.application.admin.dto.setting.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "관리자 - 시스템 설정", description = "시스템 설정 관리 API (관리자 전용)")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemSettingController {

  private final AdminSystemSettingService adminSystemSettingService;

  @Operation(summary = "전체 시스템 설정 조회", description = "모든 시스템 설정을 카테고리와 키 순으로 정렬하여 조회합니다")
  @GetMapping
  public ResponseEntity<ApiResponse<SystemSettingListResponse>> getAllSettings() {
    log.info("전체 시스템 설정 조회 요청");

    SystemSettingListResponse response = adminSystemSettingService.getAllSettings();

    ApiResponse<SystemSettingListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "시스템 설정 목록을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 카테고리별 시스템 설정 조회
   * GET /api/admin/settings/category/{category}
   */
  @GetMapping("/category/{category}")
  public ResponseEntity<ApiResponse<SystemSettingListResponse>> getSettingsByCategory(
      @PathVariable String category
  ) {
    log.info("카테고리별 시스템 설정 조회 요청: category={}", category);

    SystemSettingListResponse response = adminSystemSettingService.getSettingsByCategory(category.toUpperCase());

    ApiResponse<SystemSettingListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        category + " 카테고리 설정을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 특정 설정 조회
   * GET /api/admin/settings/{settingKey}
   */
  @GetMapping("/{settingKey}")
  public ResponseEntity<ApiResponse<SystemSettingResponse>> getSetting(
      @PathVariable String settingKey
  ) {
    log.info("특정 시스템 설정 조회 요청: settingKey={}", settingKey);

    SystemSettingResponse response = adminSystemSettingService.getSetting(settingKey);

    ApiResponse<SystemSettingResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "시스템 설정을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  @Operation(summary = "시스템 설정 업데이트", description = "특정 설정의 값과 설명을 업데이트합니다. 수정 불가능한 설정은 변경할 수 없습니다.")
  @PutMapping("/{settingKey}")
  public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
      @PathVariable String settingKey,
      @Valid @RequestBody SystemSettingUpdateCommand command
  ) {
    log.info("시스템 설정 업데이트 요청: settingKey={}, newValue={}",
        settingKey, command.settingValue());

    SystemSettingResponse response = adminSystemSettingService.updateSetting(settingKey, command);

    ApiResponse<SystemSettingResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "시스템 설정이 업데이트되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 새 설정 생성
   * POST /api/admin/settings
   */
  @PostMapping
  public ResponseEntity<ApiResponse<SystemSettingResponse>> createSetting(
      @Valid @RequestBody SystemSettingCreateCommand command
  ) {
    log.info("새 시스템 설정 생성 요청: settingKey={}, value={}",
        command.settingKey(), command.settingValue());

    SystemSettingResponse response = adminSystemSettingService.createSetting(command);

    ApiResponse<SystemSettingResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "새 시스템 설정이 생성되었습니다."
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  /**
   * 설정 삭제
   * DELETE /api/admin/settings/{settingKey}
   */
  @DeleteMapping("/{settingKey}")
  public ResponseEntity<ApiResponse<Void>> deleteSetting(
      @PathVariable String settingKey
  ) {
    log.info("시스템 설정 삭제 요청: settingKey={}", settingKey);

    adminSystemSettingService.deleteSetting(settingKey);

    ApiResponse<Void> apiResponse = new ApiResponse<>(
        true,
        null,
        "시스템 설정이 삭제되었습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 카테고리별 설정 통계
   * GET /api/admin/settings/stats
   */
  @GetMapping("/stats")
  public ResponseEntity<ApiResponse<Map<String, Long>>> getSettingsStats() {
    log.info("시스템 설정 통계 조회 요청");

    Map<String, Long> stats = adminSystemSettingService.getSettingsStatsByCategory();

    ApiResponse<Map<String, Long>> apiResponse = new ApiResponse<>(
        true,
        stats,
        "시스템 설정 통계를 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 편의용 API - 비즈니스 설정만 조회
   * GET /api/admin/settings/business
   */
  @GetMapping("/business")
  public ResponseEntity<ApiResponse<SystemSettingListResponse>> getBusinessSettings() {
    log.info("비즈니스 설정 조회 요청");

    SystemSettingListResponse response = adminSystemSettingService.getSettingsByCategory("BUSINESS");

    ApiResponse<SystemSettingListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "비즈니스 설정을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * 편의용 API - 시스템 설정만 조회
   * GET /api/admin/settings/system
   */
  @GetMapping("/system")
  public ResponseEntity<ApiResponse<SystemSettingListResponse>> getSystemSettings() {
    log.info("시스템 설정 조회 요청");

    SystemSettingListResponse response = adminSystemSettingService.getSettingsByCategory("SYSTEM");

    ApiResponse<SystemSettingListResponse> apiResponse = new ApiResponse<>(
        true,
        response,
        "시스템 설정을 조회했습니다."
    );

    return ResponseEntity.ok(apiResponse);
  }
}