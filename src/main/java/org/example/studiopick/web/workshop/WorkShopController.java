package org.example.studiopick.web.workshop;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.WorkShopService;
import org.example.studiopick.application.workshop.dto.WorkShopDetailDto;
import org.example.studiopick.application.workshop.dto.WorkShopListResponse;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class WorkShopController {

  private final WorkShopService workShopService;

  @GetMapping
  public ResponseEntity<ApiSuccessResponse<WorkShopListResponse>> getClasses(
      @RequestParam Long studioId,
      @RequestParam String status,
      @RequestParam String date
  ) {
    WorkShopListResponse response = workShopService.getWorkShopList(status, date);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiSuccessResponse<WorkShopDetailDto>> getClassDetail(@PathVariable Long id) {
    WorkShopDetailDto response = workShopService.getWorkShopDetail(id);
    return ResponseEntity.ok(new ApiSuccessResponse<>(response));
  }
}
