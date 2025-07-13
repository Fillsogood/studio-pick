package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.ClassReservationService;
import org.example.studiopick.application.classes.dto.UserClassReservationListResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/class-reservations")
public class ClassReservationController {

  private final ClassReservationService classReservationService;

  @GetMapping
  public ResponseEntity<ApiSuccessResponse<UserClassReservationListResponse>> getMyClassReservations(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestParam(defaultValue = "confirmed") String status
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    UserClassReservationListResponse response = classReservationService.getUserReservations(userId, status);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @PatchMapping("/{id}/cancel")
  public ApiSuccessResponse<Void> cancelReservation(
      @PathVariable("id") Long reservationId,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    // 토큰에서 직접 사용자 ID 추출
    Long userId = userPrincipal.getUserId();
    classReservationService.cancelReservation(reservationId, userId);
    return new ApiSuccessResponse<>(null, "클래스 예약이 취소되었습니다.");
  }
}
