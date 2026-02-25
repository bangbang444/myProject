package bangbang.gourmet.social.dto;

public record ReviewLikeResponse(
        boolean isLiked,
        long likeCount
) {
    public static ReviewLikeResponse of(boolean isLiked, long likeCount) {
        return new ReviewLikeResponse(isLiked, likeCount);
    }
}
