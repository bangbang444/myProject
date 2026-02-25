package bangbang.gourmet.review.dto;

import bangbang.gourmet.review.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String nickname,
        // String userProfileImage, // 나중에 프로필 이미지 추가 시 사용
        LocalDateTime createdAt,
        boolean isMine
) {
    public static CommentResponse of(Comment comment, Long currentUserId) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getNickname(),
                comment.getCreatedDate(),
                comment.getUser().getId().equals(currentUserId)
        );
    }
}
