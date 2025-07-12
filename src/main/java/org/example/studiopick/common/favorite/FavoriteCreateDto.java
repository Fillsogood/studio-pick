package org.example.studiopick.common.favorite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteCreateDto {

    @NotBlank(message = "타입은 필수입니다.")
    private String targetType; // "studio" or "class"

    @NotNull(message = "대상 ID는 필수입니다.")
    private Long targetId;
}
