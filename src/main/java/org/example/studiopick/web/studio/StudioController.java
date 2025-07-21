package org.example.studiopick.web.studio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.studiopick.application.studio.StudioService;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.security.CustomUserDetailsService;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
@Slf4j
public class StudioController {

  private final StudioService studioService;
  private final CustomUserDetailsService userDetailsService;

  // 1. 전체 스튜디오 조회
  @GetMapping
  public ResponseEntity<ApiSuccessResponse<StudioListResponse>> getStudios(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String location,
      @RequestParam(defaultValue = "popular") String sort
  ) {
    StudioListResponse response = studioService.searchStudios(location, sort, page, limit);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  // 2. 검색 필터 적용 조회
  @GetMapping("/search")
  public ResponseEntity<ApiSuccessResponse<StudioSearchResponse>> searchStudios(
      @RequestParam String keyword,
      @RequestParam(required = false) String location,
      @RequestParam(defaultValue = "rating") String sort
  ) {
    List<StudioSearchDto> studios = studioService.searchByKeyword(keyword, location, sort);
    return ResponseEntity.ok(
        new ApiSuccessResponse<>(new StudioSearchResponse(studios, studios.size()))
    );
  }

  // 3. 스튜디오 상세 조회
  @GetMapping("/{studioId}")
  public ResponseEntity<ApiSuccessResponse<StudioDetailDto>> getStudioDetail(@PathVariable Long studioId) {
    StudioDetailDto detail = studioService.findById(studioId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(detail));
  }

  // 4. 스튜디오 갤러리 이미지 조회
  @GetMapping("/{studioId}/gallery")
  public ResponseEntity<ApiSuccessResponse<List<GalleryDto>>> getGallery(@PathVariable Long studioId) {
    List<GalleryDto> images = studioService.gallery(studioId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(images));
  }


  // 5. 스튜디오 요금 정보 조회
  @GetMapping("/{studioId}/pricing")
  public ResponseEntity<ApiSuccessResponse<PricingDto>> getPricing(@PathVariable Long studioId) {
    PricingDto pricing = studioService.pricing(studioId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(pricing));
  }

  // 6. 현재 이용 가능한 스튜디오 조회
  @GetMapping("/available-now")
  public ResponseEntity<ApiSuccessResponse<List<StudioAvailableDto>>> getAvailableNow() {
    List<StudioAvailableDto> response = studioService.availableNow();
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  // 스튜디오 개설
  @PostMapping
  public ResponseEntity<ApiResponse<StudioDetailDto>> createStudio(
      @RequestBody StudioCreateRequest request,
      @AuthenticationPrincipal Long userId
  ) {
    StudioDetailDto response = studioService.createStudio(request, userId);
    return ResponseEntity.ok(new ApiResponse<>(true, response, "스튜디오가 성공적으로 개설되었습니다"));
  }

  // 스튜디오 편집(정보수정)
  @PatchMapping("/{studioId}")
  public ResponseEntity<ApiSuccessResponse<StudioDetailDto>> updateStudio(
      @PathVariable Long studioId,
      @RequestBody StudioDetailDto request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getId();
    StudioDetailDto result = studioService.updateStudio(studioId, request, userId);
    log.info("✅ 스튜디오 요청 도착: {}", studioId); // ← 이거 추가!
    log.info("✅ 스튜디오 요청 도착: {}", request); // ← 이거 추가!
    log.info("✅ 스튜디오 요청 도착: {}", userId); // ← 이거 추가!
    return ResponseEntity.ok(new ApiSuccessResponse<>(result, "스튜디오 정보가 수정되었습니다."));
  }

  // 스튜디오 비활성화
  @PatchMapping("/{studioId}/deactivate")
  public ResponseEntity<ApiSuccessResponse<Void>> deactivateStudio(@PathVariable Long studioId) {
    studioService.deactivateStudio(studioId);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "스튜디오가 비활성화되었습니다."));
  }

  /**
   * 스튜디오 운영 신청
   */
//  @PostMapping("/rental")
//  public ResponseEntity<?> apply(
//      @RequestBody SpaceRentalApplicationRequest request,
//      @AuthenticationPrincipal Long userId
//  ) {
//    StudioApplicationResponse result = studioService.studioRental(request, userId);
//    return ResponseEntity.status(HttpStatus.CREATED)
//        .body(new ApiResponse<>(true, result, "스튜디오 운영 신청이 접수되었습니다"));
//  }

  @PostMapping("/rental")
  public ResponseEntity<?> applyStudio(
      @RequestBody SpaceRentalApplicationRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getId();
    log.info("✅ 스튜디오 신청 요청 도착: {}", request); // ← 이거 추가!
    log.info("✅ 스튜디오 신청 요청 도착: {}", userId); // ← 이거 추가!
    studioService.studioRental(request, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * 스튜디오 운영 신청 상태 조회
   */
  @GetMapping("/{studioId}/application-status")
  public ResponseEntity<ApiResponse<StudioApplicationDetailResponse>> getApplicationStatus(
      @PathVariable Long studioId
  ) {
    StudioApplicationDetailResponse response = studioService.studioRentalApplicationStatus(studioId);
    return ResponseEntity.ok(new ApiResponse<>(true, response, null));
  }

  /**
  *  스튜디오 이미지 s3업로드
  */
  @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<String>>> uploadStudioImages(
      @RequestPart("images") MultipartFile[] images
  ) {
    List<String> imageUrls = studioService.uploadStudioImages(images);
    return ResponseEntity.ok(new ApiResponse<>(true, imageUrls, "업로드 완료"));
  }

  @GetMapping("/my")
  public ResponseEntity<ApiSuccessResponse<List<StudioDto>>> getMyStudios(
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getId();
    List<StudioDto> result = studioService.getMyStudios(userId);
    log.info("✅ 스튜디오: {}", result); // ← 이거 추가!
    log.info("✅ 스튜디오: {}", userId); // ← 이거 추가!
    return ResponseEntity.ok(new ApiSuccessResponse<>(result, "내 스튜디오 목록 조회 성공"));
  }

}