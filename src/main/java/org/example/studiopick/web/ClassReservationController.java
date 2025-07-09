package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.classes.ClassReservationService;
import org.example.studiopick.application.classes.dto.ClassReservationCancelResponse;
import org.example.studiopick.application.classes.dto.UserClassReservationListResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/class-reservations")
public class ClassReservationController {

  private final ClassReservationService classReservationService;

  @GetMapping
  public ResponseEntity<ApiSuccessResponse<UserClassReservationListResponse>> getUserReservations(
      @RequestParam Long userId,
      @RequestParam(defaultValue = "confirmed") String status
  ) {
    UserClassReservationListResponse response = classReservationService.getUserReservations(userId, status);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @PatchMapping("/{id}/cancel")
  public ApiSuccessResponse<Void> cancelReservation(
      @PathVariable("id") Long reservationId,
      @RequestParam("userId") Long userId  // 마이페이지라면 토큰에서 꺼내는 구조로 대체 가능
  ) {
    classReservationService.cancelReservation(reservationId, userId);
    return new ApiSuccessResponse<>(null, "클래스 예약이 취소되었습니다.");
  }
}