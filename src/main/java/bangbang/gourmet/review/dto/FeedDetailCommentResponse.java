package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Comment;

import java.time.LocalDateTime;

public record FeedDetailCommentResponse(
        String user,
        String text,
        LocalDateTime date
) {
    public static FeedDetailCommentResponse from(Comment comment) {
        return new FeedDetailCommentResponse(
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedDate()
        );
    }
}