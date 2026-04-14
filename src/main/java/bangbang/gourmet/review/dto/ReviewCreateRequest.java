package bangbang.gourmet.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull(message = "맛 평점은 필수입니다.")
        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        Double tasteRating,

        @NotNull(message = "분위기 평점은 필수입니다.")
        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        Double atmosphereRating,

        @NotNull(message = "서비스 평점은 필수입니다.")
        @DecimalMin(value = "0.0") @DecimalMax(value = "5.0")
        Double serviceRating,

        String content,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic
) {
}
