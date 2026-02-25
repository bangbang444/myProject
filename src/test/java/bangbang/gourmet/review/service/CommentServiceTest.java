package bangbang.gourmet.review.service;

import bangbang.gourmet.review.dto.CommentCreateResponse;
import bangbang.gourmet.review.dto.CommentResponse;
import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("댓글 작성 시 작성자의 닉네임과 내용을 포함한 응답을 반환한다")
    void createComment_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 10L;
        User user = User.builder().nickname("강병현").build();
        Review review = Review.builder().content("리뷰 내용").build();
        Comment comment = Comment.builder().content("댓글입니다").user(user).review(review).build();

        ReflectionTestUtils.setField(comment, "id", 100L);
        ReflectionTestUtils.setField(comment, "createdDate", LocalDateTime.now());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentCreateResponse response = commentService.createComment(userId, reviewId, "댓글입니다");

        // then
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.nickname()).isEqualTo("강병현");
        assertThat(response.content()).isEqualTo("댓글입니다");
    }

    @Test
    @DisplayName("댓글 목록 조회 시 본인이 쓴 댓글은 isMine이 true여야 한다")
    void getComments_IsMine_Check() {
        // given
        Long userId = 1L;
        Long reviewId = 10L;
        User user = User.builder().nickname("강병현").build();
        ReflectionTestUtils.setField(user, "id", userId);

        Review review = Review.builder().build();
        Comment myComment = Comment.builder().content("내 댓글").user(user).review(review).build();

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(commentRepository.findAllByReviewOrderByCreatedDate(review)).willReturn(List.of(myComment));

        // when
        List<CommentResponse> responses = commentService.getComments(reviewId, userId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).isMine()).isTrue(); // 이 부분이 핵심!
    }
}