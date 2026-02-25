package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewFeedResponse(
        Long reviewId,
        Long userId,
        String restaurantName,   // 식당 이름
        String category,         // 식당 분류
        String nickname,         // 작성자 닉네임
        String profileImageUrl,  // 작성자 프로필 (나중을 위해)
        String content,          // 리뷰 내용
        Double rating,           // 평점
        List<String> imageUrls,  // 리뷰 이미지 리스트
        long likeCount,          // 좋아요 수
        long commentCount,       // 댓글 수
        boolean isLiked,
        LocalDateTime createdAt  // 작성 시간
) {
    public static ReviewFeedResponse of(Review review, long likeCount, long commentCount, boolean isLiked) {
        return new ReviewFeedResponse(
                review.getId(),
                review.getUser().getId(),
                review.getRestaurant().getRestaurantName(),
                review.getRestaurant().getMainCategory().toString(), // Enum 타입일 경우 대비
                review.getUser().getNickname(),
                null, // TODO: User의 profileImageKey를 이용해 전체 URL을 생성하는 로직 구현 필요
                review.getContent(),
                review.getRating(),
                review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList(),
                likeCount, // 좋아요 기능 구현 전 기본값
                commentCount, // 댓글 기능 구현 전 기본값
                isLiked,
                review.getCreatedDate()
        );
    }
}
