package org.example.studiopick.web.artwork;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.application.artwork.ArtworkLikeService;
import org.example.studiopick.domain.user.User;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArtworkLikeController {

    private final ArtworkLikeService artworkLikeService;
    private final UserService userService;

    @PostMapping("/api/artworks/{artworkId}/like")
    public ResponseEntity<Void> toggleLike(
            @PathVariable Long artworkId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        User user = userService.getById(userId);
        
        artworkLikeService.toggleLike(artworkId, user);
        return ResponseEntity.ok().build();
    }
}
