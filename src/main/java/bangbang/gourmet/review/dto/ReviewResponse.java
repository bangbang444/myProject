package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        String nickname, // 작성자
        Double rating,
        String content,
        List<String> imageUrls, // 리뷰에 달린 모든 이미지 URL
        LocalDateTime createdAt // ??
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList(),
                review.getCreatedDate()
        );
    }
}
