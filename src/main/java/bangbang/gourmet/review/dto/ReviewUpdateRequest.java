package bangbang.gourmet.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReviewUpdateRequest(
        String content,
        @NotNull(message = "평점은 필수 입력 항목입니다.")
        @DecimalMin(value = "0.0", message = "평점은 0.0점 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "평점은 5.0점 이하이어야 합니다.")
        Double rating,
        List<Long> deleteImageIds // 삭제할 기존 이미지 ID들
) {
}
