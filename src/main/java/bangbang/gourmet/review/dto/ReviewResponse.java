package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        String nickname,
        Double tasteRating,
        Double atmosphereRating,
        Double serviceRating,
        String content,
        String category,
        Boolean isPublic,
        List<ReviewImageDetail> images,
        LocalDateTime createdAt,
        long authorReviewCount,
        Double authorAverageRating
) {
    public record ReviewImageDetail(
            Long imageId,
            String imageUrl
    ) {
        public static ReviewImageDetail from(ReviewImage reviewImage) {
            return new ReviewImageDetail(reviewImage.getId(), reviewImage.getImageUrl());
        }
    }

    public static ReviewResponse of(Review review, long authorReviewCount, Double authorAverageRating) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getNickname(),
                review.getTasteRating(),
                review.getAtmosphereRating(),
                review.getServiceRating(),
                review.getContent(),
                review.getCategory(),
                review.getIsPublic(),
                review.getImages().stream()
                        .map(ReviewImageDetail::from)
                        .toList(),
                review.getCreatedDate(),
                authorReviewCount,
                authorAverageRating
        );
    }
}
