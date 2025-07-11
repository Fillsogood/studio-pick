package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.StudioService;
import org.example.studiopick.application.studio.dto.*;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studios")
@RequiredArgsConstructor
public class StudioController {

  private final StudioService studioService;

  // 1. 전체 스튜디오 조회
  @GetMapping
  public ResponseEntity<ApiSuccessResponse<StudioListResponse>> getStudios(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "popular") String sort
  ) {
    StudioListResponse response = studioService.searchStudios(category, location, sort, page, limit);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  // 2. 검색 필터 적용 조회
  @GetMapping("/search")
  public ResponseEntity<ApiSuccessResponse<StudioSearchResponse>> searchStudios(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String location,
      @RequestParam(defaultValue = "rating") String sort
  ) {
    List<StudioSearchDto> studios = studioService.searchByKeyword(q, location, sort);
    return ResponseEntity.ok(
        new ApiSuccessResponse<>(new StudioSearchResponse(studios, studios.size()))
    );
  }

  // 3. 스튜디오 상세 조회
  @GetMapping("/{id}")
  public ResponseEntity<ApiSuccessResponse<StudioDetailDto>> getStudioDetail(@PathVariable Long id) {
    StudioDetailDto detail = studioService.findById(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(detail));
  }

  // 4. 스튜디오 갤러리 이미지 조회
  @GetMapping("/{id}/gallery")
  public ResponseEntity<ApiSuccessResponse<Map<String, List<GalleryDto>>>> getGallery(@PathVariable Long id) {
    List<GalleryDto> images = studioService.gallery(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(Map.of("images", images)));
  }

  @PatchMapping("/{id}/gallery")
  public ResponseEntity<ApiSuccessResponse<Void>> updateGalleryOrder(
      @PathVariable Long id,
      @RequestBody List<StudioGalleryOrderUpdate> requestList
  ) {
    studioService.updateGalleryOrder(id, requestList);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null));
  }

  // 5. 스튜디오 요금 정보 조회
  @GetMapping("/{id}/pricing")
  public ResponseEntity<ApiSuccessResponse<PricingDto>> getPricing(@PathVariable Long id) {
    PricingDto pricing = studioService.pricing(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(pricing));
  }

  // 6. 현재 이용 가능한 스튜디오 조회
  @GetMapping("/available-now")
  public ResponseEntity<ApiSuccessResponse<AvailableStudiosResponse>> getAvailableNow() {
    AvailableStudiosResponse response = new AvailableStudiosResponse(studioService.availableNow());
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  // 스튜디오 개설
  @PostMapping
  public ResponseEntity<ApiResponse<StudioCreateResponse>> createStudio(
      @ModelAttribute StudioCreateRequest request
  ) {
    StudioCreateResponse response = studioService.createStudio(request);
    return ResponseEntity.ok(new ApiResponse<>(true, response, "스튜디오가 성공적으로 개설되었습니다"));
  }

  // 스튜디오 편집(정보수정)
  @PatchMapping("/{id}")
  public ResponseEntity<ApiSuccessResponse<Void>> updateStudio(
      @PathVariable Long id,
      @RequestBody StudioUpdateRequest request
  ) {
    studioService.updateStudio(id, request);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "스튜디오 정보가 수정되었습니다."));
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiSuccessResponse<Void>> deactivateStudio(@PathVariable Long id) {
    studioService.deactivateStudio(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(null, "스튜디오가 비활성화되었습니다."));
  }
}