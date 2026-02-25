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
        List<ReviewImageDetail> images, // 리뷰 이미지 리스트
        LocalDateTime createdAt // ??
) {
    public record ReviewImageDetail(
            Long imageId,
            String imageUrl
    ) {
        public static ReviewImageDetail from(ReviewImage reviewImage) {
            return new ReviewImageDetail(reviewImage.getId(), reviewImage.getImageUrl());
        }
    }

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getImages().stream()
                        .map(ReviewImageDetail::from)
                        .toList(),
                review.getCreatedDate()
        );
    }
}
