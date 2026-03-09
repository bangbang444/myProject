package bangbang.gourmet.review.dto;

import bangbang.gourmet.common.util.RatingUtils;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;

import java.util.List;

public record FeedDetailResponse(
        Long id,
        String name,
        AuthorInfo author,
        List<String> images,
        String desc,
        String tags,
        Double rating,
        String location,
        StatsInfo stats,
        long likeCount,
        boolean isLiked,
        List<FeedDetailCommentResponse> comments
) {
    public record AuthorInfo(String name, String avatar) {}

    public record StatsInfo(Double taste, Double atmosphere, Double service) {}

    public static FeedDetailResponse of(Review review, long likeCount, boolean isLiked,
                                        List<FeedDetailCommentResponse> comments) {
        String mainCategory = review.getRestaurant().getMainCategory();
        String sigungu = review.getRestaurant().getSigungu();
        String sido = review.getRestaurant().getSido();

        String tags = (mainCategory != null ? mainCategory + " • " : "") + (sigungu != null ? sigungu : "");
        String location = (sigungu != null ? sigungu : "") + (sido != null ? ", " + sido : "");

        return new FeedDetailResponse(
                review.getId(),
                review.getRestaurant().getRestaurantName(),
                new AuthorInfo(
                        review.getUser().getNickname(),
                        review.getUser().getProfileImageKey()
                ),
                review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .toList(),
                review.getContent(),
                tags,
                RatingUtils.roundToOneDecimal(review.getAverageRating()),
                location,
                new StatsInfo(
                        review.getTasteRating(),
                        review.getAtmosphereRating(),
                        review.getServiceRating()
                ),
                likeCount,
                isLiked,
                comments
        );
    }
}