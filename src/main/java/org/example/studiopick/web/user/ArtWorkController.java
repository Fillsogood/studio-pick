package org.example.studiopick.web.user;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.dto.ApiResponse;
import org.example.studiopick.common.dto.artwork.ArtworkDetailResponseDto;
import org.example.studiopick.common.dto.artwork.ArtworkFeedDto;
import org.example.studiopick.domain.artwork.Artwork;
import org.example.studiopick.domain.artwork.ArtworkService;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
