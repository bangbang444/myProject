package bangbang.gourmet.review.dto;

import java.util.List;

public record ReviewSimpleResponse(
        Long id,
        String nickname,
        Double averageRating,
        String content,
        List<String> imageUrls,
        String createdAt
) {}
