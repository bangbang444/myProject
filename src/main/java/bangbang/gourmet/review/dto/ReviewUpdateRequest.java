package bangbang.gourmet.review.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewUpdateRequest(
        String content,
        Double rating,
        List<Long> deleteImageIds // 삭제할 기존 이미지 ID들
) {
}
