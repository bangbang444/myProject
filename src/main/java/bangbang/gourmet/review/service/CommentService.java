package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.ForbiddenException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.review.dto.CommentCreateResponse;
import bangbang.gourmet.review.dto.CommentResponse;
import bangbang.gourmet.review.dto.CommentUpdateRequest;
import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public CommentCreateResponse createComment(Long userId, Long reviewId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .review(review)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentCreateResponse.from(savedComment);
    }

    public List<CommentResponse> getComments(Long reviewId, Long userId) {
        if(!userRepository.existsById(userId)){
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        return commentRepository.
                findAllByReviewOrderByCreatedDate(review).stream()
                .map(comment -> CommentResponse.of(comment, userId))
                .toList();
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        // 엔티티 업데이트 (더티 체킹)
        comment.update(request.content());
    }
}
