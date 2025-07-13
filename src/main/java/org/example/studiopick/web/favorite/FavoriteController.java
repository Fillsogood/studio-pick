package org.example.studiopick.web.favorite;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.favorite.FavoriteService;
import org.example.studiopick.application.user.service.UserService;
import org.example.studiopick.common.favorite.FavoriteCreateDto;
import org.example.studiopick.common.favorite.FavoriteResponseDto;
import org.example.studiopick.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "UserFavorite", description = "유저 즐겨찾기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    @PostMapping
    @Operation(summary = "즐겨찾기 추가")
    public FavoriteResponseDto addFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @Valid @RequestBody FavoriteCreateDto dto) {
        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        var user = userService.getById(userId);
        return favoriteService.addFavorite(user, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "즐겨찾기 삭제")
    public void deleteFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal,
                               @PathVariable Long id) {
        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        var user = userService.getById(userId);
        favoriteService.deleteFavorite(user, id);
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 목록 조회")
    public Page<FavoriteResponseDto> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                  @RequestParam String type,
                                                  Pageable pageable) {
        // 토큰에서 직접 사용자 ID 추출하여 User 객체 조회
        Long userId = userPrincipal.getUserId();
        var user = userService.getById(userId);
        return favoriteService.getFavorites(user, type, pageable);
    }
}
