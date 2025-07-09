package org.example.studiopick.web.user;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.domain.artwork.ArtworkLikeService;
import org.example.studiopick.domain.user.entity.User;
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

        User user = userService.getById(userPrincipal.getId());
        artworkLikeService.toggleLike(artworkId, user);
        return ResponseEntity.ok().build();
    }
}
