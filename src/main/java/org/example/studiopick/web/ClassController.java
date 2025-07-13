package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.ClassService;
import org.example.studiopick.application.classes.dto.ClassDetailDto;
import org.example.studiopick.application.classes.dto.ClassListResponse;
import org.example.studiopick.application.classes.dto.ClassReservationRequest;
import org.example.studiopick.application.classes.dto.ClassReservationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

  private final ClassService classService;

  @GetMapping
  public ResponseEntity<ApiSuccessResponse<ClassListResponse>> getClasses(
      @RequestParam Long studioId,
      @RequestParam String status,
      @RequestParam String date
  ) {
    ClassListResponse response = classService.getClassList(studioId, status, date);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiSuccessResponse<ClassDetailDto>> getClassDetail(@PathVariable Long id) {
    ClassDetailDto response = classService.getClassDetail(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @PostMapping("/{id}/reservations")
  public ResponseEntity<ApiResponse<ClassReservationResponse>> reserveClass(
      @PathVariable Long id,
      @RequestBody ClassReservationRequest request,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    ClassReservationResponse response = classService.reserveClass(id, userId, request.participants());
    return ResponseEntity.ok(new ApiResponse<>(true, response, "클래스 예약이 완료되었습니다"));
  }
}
