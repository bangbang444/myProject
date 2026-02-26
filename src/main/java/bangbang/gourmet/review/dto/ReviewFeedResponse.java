package bangbang.gourmet.review.dto;

import bangbang.gourmet.common.util.RatingUtils;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewFeedResponse(
        Long reviewId,
        Long userId,
        String restaurantName,
        String category,
        String nickname,
        String profileImageUrl,
        String content,
        Double averageRating,
        List<String> imageUrls,
        long likeCount,
        long commentCount,
        boolean isLiked,
        LocalDateTime createdAt
) {
    public static ReviewFeedResponse of(Review review, long likeCount, long commentCount, boolean isLiked) {
        return new ReviewFeedResponse(
                review.getId(),
                review.getUser().getId(),
                review.getRestaurant().getRestaurantName(),
                review.getRestaurant().getMainCategory().toString(),
                review.getUser().getNickname(),
                null, // TODO: User의 profileImageKey를 이용해 전체 URL을 생성하는 로직 구현 필요
                review.getContent(),
                RatingUtils.roundToOneDecimal(review.getAverageRating()),
                review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList(),
                likeCount,
                commentCount,
                isLiked,
                review.getCreatedDate()
        );
    }
}
