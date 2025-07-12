package org.example.studiopick.web.favorite;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.favorite.FavoriteService;
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

    @PostMapping
    @Operation(summary = "즐겨찾기 추가")
    public FavoriteResponseDto addFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                           @Valid @RequestBody FavoriteCreateDto dto) {
        return favoriteService.addFavorite(userPrincipal.getUser(), dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "즐겨찾기 삭제")
    public void deleteFavorite(@AuthenticationPrincipal UserPrincipal userPrincipal,
                               @PathVariable Long id) {
        favoriteService.deleteFavorite(userPrincipal.getUser(), id);
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 목록 조회")
    public Page<FavoriteResponseDto> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                  @RequestParam String type,
                                                  Pageable pageable) {
        return favoriteService.getFavorites(userPrincipal.getUser(), type, pageable);
    }
}