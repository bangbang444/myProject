package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Comment;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long id,
        String content,
        String nickname,
        LocalDateTime createdAt
) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getNickname(),
                comment.getCreatedDate()
        );
    }
}
