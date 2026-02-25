package bangbang.gourmet.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewCreateRequest(
        @NotNull(message = "평점은 필수입니다.")
        @Min(0) @Max(5)
        Double rating,
        String content,
        List<MultipartFile> images
) {
}
