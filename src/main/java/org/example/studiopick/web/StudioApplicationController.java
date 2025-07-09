package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.StudioService;
import org.example.studiopick.application.studio.dto.StudioApplicationDetailResponse;
import org.example.studiopick.application.studio.dto.StudioApplicationRequest;
import org.example.studiopick.application.studio.dto.StudioApplicationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studio-applications")
public class StudioApplicationController {

  private final StudioService studioService;

  /**
   * 스튜디오 운영 신청
   */
  @PostMapping
  public ResponseEntity<ApiResponse<StudioApplicationResponse>> applyStudio(
      @RequestBody StudioApplicationRequest request
  ) {
    StudioApplicationResponse response = studioService.applyStudio(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ApiResponse<>(true, response, "스튜디오 운영 신청이 접수되었습니다"));
  }

  /**
   * 스튜디오 운영 신청 상태 조회
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<StudioApplicationDetailResponse>> getApplicationStatus(
      @PathVariable Long id
  ) {
    StudioApplicationDetailResponse response = studioService.getApplicationStatus(id);
    return ResponseEntity.ok(new ApiResponse<>(true, response, null));
  }
}
