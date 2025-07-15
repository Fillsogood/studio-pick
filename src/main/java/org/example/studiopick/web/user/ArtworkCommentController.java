package org.example.studiopick.web.user;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.dto.artwork.ArtworkCommentRequestDto;
import org.example.studiopick.common.dto.artwork.ArtworkCommentResponseDto;
import org.example.studiopick.domain.artwork.ArtworkCommentService;
import org.example.studiopick.domain.user.entity.User;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArtworkCommentController {
    private final ArtworkCommentService artworkCommentService;
    private final UserService userService;

    @PostMapping("/api/artworks/{artworkId}/comments")
    public ResponseEntity<ArtworkCommentResponseDto> addComment(
            @PathVariable Long artworkId,
            @RequestBody ArtworkCommentRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        User user = userService.getById(userId);
        
        ArtworkCommentResponseDto response =
                artworkCommentService.createComment(artworkId, user, request.getComment());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/artworks/{artworkId}/comments")
    public ResponseEntity<List<ArtworkCommentResponseDto>> getComments(
            @PathVariable Long artworkId) {

        List<ArtworkCommentResponseDto> comments = artworkCommentService.getCommentsByArtworkId(artworkId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/api/artworks/{artworkId}/comments/{commentId}")
    public ResponseEntity<ArtworkCommentResponseDto> updateComment(
            @PathVariable Long artworkId,
            @PathVariable Long commentId,
            @RequestBody ArtworkCommentRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        User user = userService.getById(userId);
        
        ArtworkCommentResponseDto response =
                artworkCommentService.updateComment(artworkId, commentId, user, request.getComment());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/artworks/{artworkId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long artworkId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        User user = userService.getById(userId);
        
        artworkCommentService.deleteComment(artworkId, commentId, user);
        return ResponseEntity.noContent().build();
    }
}
