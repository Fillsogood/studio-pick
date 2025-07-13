package org.example.studiopick.web;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.studio.StudioService;
import org.example.studiopick.application.studio.dto.SpaceRentalApplicationRequest;
import org.example.studiopick.application.studio.dto.StudioApplicationDetailResponse;
import org.example.studiopick.application.studio.dto.StudioApplicationResponse;
import org.example.studiopick.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/space-rental-applications")
public class SpaceRentalApplicationController {

    private final StudioService studioService;

    /**
     * 공간 대여 운영 신청
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<StudioApplicationResponse>> applySpaceRental(
        @ModelAttribute SpaceRentalApplicationRequest request
    ) {
        StudioApplicationResponse response = studioService.applySpaceRental(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, response, "공간 대여 운영 신청이 접수되었습니다"));
    }

    /**
     * 공간 대여 신청 상태 조회
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<StudioApplicationDetailResponse>> getApplicationStatus(
        @PathVariable Long id
    ) {
        StudioApplicationDetailResponse response = studioService.getSpaceRentalApplicationStatus(id);
        return ResponseEntity.ok(new ApiResponse<>(true, response, null));
    }
}
