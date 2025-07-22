package org.example.studiopick.web.workshop;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.workshop.dto.ClassManageItemResponseDto;
import org.example.studiopick.application.workshop.dto.WorkshopStatusUpdateRequest;
import org.example.studiopick.application.workshop.WorkShopService;
import org.example.studiopick.domain.common.dto.ApiSuccessResponse;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/host/classes")
@RequiredArgsConstructor
public class HostClassController {

    private final WorkShopService workShopService;

    /**
     * 호스트용 클래스 관리 리스트 조회
     */
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<ClassManageItemResponseDto>>> getHostClasses(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long ownerId = principal.getId();
        List<ClassManageItemResponseDto> list = workShopService.getClassManageList(ownerId);
        return ResponseEntity.ok(ApiSuccessResponse.of(list));
    }

    /**
     * 워크샵 상태를 ACTIVE/INACTIVE 으로 변경
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiSuccessResponse<Void>> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody WorkshopStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // (선택) principal.getId()로 소유자 권한 검증 로직 추가 가능
        workShopService.updateWorkshopStatus(id, request.status());
        return ResponseEntity.ok(ApiSuccessResponse.of(null));
    }
}
