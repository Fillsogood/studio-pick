package org.example.studiopick.web.workshop;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.WorkShopService;
import org.example.studiopick.application.workshop.dto.*;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class WorkShopController {

  private final WorkShopService workShopService;

  @GetMapping
  public ResponseEntity<ApiSuccessResponse<WorkShopListResponse>> getClasses(
      @RequestParam String status,
      @RequestParam(required = false) String date
  ) {
    WorkShopListResponse response = workShopService.getWorkShopList(status, date);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiSuccessResponse<WorkShopDetailDto>> getClassDetail(@PathVariable Long id) {
    WorkShopDetailDto response = workShopService.getWorkShopDetail(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  /**
   * 공방 운영 신청
   */
  @PostMapping
  public ResponseEntity<ApiSuccessResponse<WorkShopApplicationResponse>> applyWorkshop(
      @RequestBody WorkShopApplicationRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getUserId();
    WorkShopApplicationResponse response = workShopService.applyWorkshop(request, userId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response, "공방 운영 신청이 완료되었습니다."));
  }

  /**
   * 공방 신청 상태 조회
   */
  @GetMapping("/{id}/application-status")
  public ResponseEntity<ApiSuccessResponse<WorkShopApplicationDetailResponse>> getApplicationStatus(@PathVariable Long id) {
    WorkShopApplicationDetailResponse response = workShopService.getWorkshopApplicationStatus(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response, "공방 신청 상태를 조회했습니다."));
  }


  /**
   * 공방 정보 수정
   */
  @PutMapping("/{id}")
  @Operation(summary = "워크샵 정보 수정")
  public ResponseEntity<ApiSuccessResponse<WorkShopDetailDto>> updateWorkshop(
          @PathVariable Long id,
          @RequestBody WorkShopUpdateRequestDto request,
          @AuthenticationPrincipal UserPrincipal principal
  ) {
    WorkShopDetailDto updated = workShopService.updateWorkshop(
            id,
            request,
            principal.getId()
    );
    return ResponseEntity
            .ok(ApiSuccessResponse.of(updated));
  }

  /**
   * 공방 비활성화
   */
  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiSuccessResponse<Void>> deactivateWorkshop(@PathVariable Long id) {
    workShopService.deactivateWorkshop(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "공방이 비활성화되었습니다."));
  }

  @PostMapping("/{id}/activate")
  public ResponseEntity<ApiSuccessResponse<Long>> activateAndCreateWorkshop(
      @PathVariable Long id,
      @RequestBody WorkShopCreateCommand command,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long adminUserId = userPrincipal.getUserId();
    Long workshopId = workShopService.activateAndCreateWorkshop(id, command, adminUserId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(workshopId, "공방이 승인 및 활성화되었습니다."));
  }

  @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiSuccessResponse<List<String>>> uploadClassImages(
          @RequestPart("files") List<MultipartFile> files
  ) {
    List<String> urls = workShopService.uploadClassImages(files);
    return ResponseEntity.ok(new ApiSuccessResponse<>(urls, "클래스 이미지가 업로드되었습니다."));
  }

  @Operation(summary = "클래스 이미지 삭제", description = "업로드된 클래스 이미지를 S3에서 삭제합니다.")
  @DeleteMapping(value = "/images", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ApiSuccessResponse<Void>> deleteClassImages(
          @RequestBody List<String> imageUrls
  ) {
    workShopService.deleteClassImages(imageUrls);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "이미지가 삭제되었습니다."));
  }

}
