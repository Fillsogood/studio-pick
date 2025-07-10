package org.example.studiopick.web.user;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.common.dto.artwork.ArtworkDetailResponseDto;
import org.example.studiopick.common.dto.artwork.ArtworkFeedDto;
import org.example.studiopick.common.dto.artwork.ArtworkUploadRequestDto;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkService;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artworks")
public class ArtWorkController {

    private final ArtworkService artworkService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArtworks(
            @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(required = false) String hashtags
    ) {
        int offset = (page - 1) * limit;
        List<ArtworkFeedDto> artworks = artworkService.getArtworks(sort, offset, limit, hashtags);

        Map<String, Object> result = new HashMap<>();
        result.put("artworks", artworks);

        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtworkDetailResponseDto>> getArtworkById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getById(userPrincipal.getId());
        ArtworkDetailResponseDto detail = artworkService.getArtworkDetail(id, user);
        return ResponseEntity.ok(new ApiResponse<>(true, detail, null));
    }

    // 1. 이미지 업로드 API
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadArtworkImages(
            @RequestPart("images") MultipartFile[] images) {

        if (images.length > 10) {
            throw new IllegalArgumentException("최대 10장의 이미지만 업로드 가능합니다.");
        }

        List<String> uploadedUrls = new ArrayList<>();
        for (MultipartFile file : images) {
            validateImageFile(file); // 용량/확장자 검사
            String url = artworkService.uploadToS3(file); // 내부에서 S3Uploader 호출
            uploadedUrls.add(url);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imageUrls", uploadedUrls);

        return ResponseEntity.ok(new ApiResponse<>(true, result, null));
    }

    // 2. 작품 저장 API
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadArtwork(
            @RequestBody ArtworkUploadRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        Long artworkId = artworkService.saveArtwork(dto, userId); // 저장 후 ID 반환

        Map<String, Object> result = new HashMap<>();
        result.put("artworkId", artworkId);

        return ResponseEntity.ok(new ApiResponse<>(true, result, "작품이 업로드되었습니다."));
    }

    // 3. 이미지 유효성 검사 메서드
    private void validateImageFile(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 용량은 10MB를 초과할 수 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            throw new IllegalArgumentException("이미지 형식은 jpg, png, webp만 허용됩니다.");
        }
    }

    // 4. 작품 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateArtwork(
            @PathVariable Long id,
            @RequestBody ArtworkUploadRequestDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        artworkService.updateArtwork(id, userPrincipal.getUserId(), dto);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "작품이 수정되었습니다."));
    }

    // 5. 작품 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArtwork(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        artworkService.deleteArtwork(id, userPrincipal.getUserId());
        return ResponseEntity.ok(new ApiResponse<>(true, null, "작품이 삭제되었습니다."));
    }


}
