package bangbang.gourmet.review.dto;

import java.util.List;

public record ReviewSimpleResponse(
        Long id,
        String nickname,
        Double rating,
        String content,
        List<String> imageUrls,
        String createdAt    // "2 days ago" 등 가공된 날짜 정보
) {}
