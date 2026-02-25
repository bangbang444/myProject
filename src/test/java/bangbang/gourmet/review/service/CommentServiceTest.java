package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.ForbiddenException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.review.dto.CommentCreateResponse;
import bangbang.gourmet.review.dto.CommentResponse;
import bangbang.gourmet.review.dto.CommentUpdateRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Test
    @DisplayName("댓글 수정 성공 테스트 - 본인이 작성한 댓글의 내용을 수정한다")
    void updateComment_Success() {
        // 💡 1. 준비 (Given)
        Long userId = 1L;
        Long commentId = 50L;

        // 가짜 유저 생성
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        // 수정 전 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .user(user)
                .content("수정 전 댓글 내용입니다.")
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글 내용입니다.");

        // Mock 설정: 리포지토리에서 해당 댓글을 찾았을 때 가짜 댓글 반환
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // 2. 실행 (When)
        commentService.updateComment(userId, commentId, request);

        // 3. 검증 (Then)
        assertThat(comment.getContent()).isEqualTo("수정된 댓글 내용입니다.");

        // 추가로 리포지토리 조회가 발생했는지 확인
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자가 아닌 유저가 수정을 시도하면 예외가 발생한다")
    void updateComment_Fail_NotOwner() {
        // Given
        Long ownerId = 1L;
        Long hackerId = 999L; // 다른 유저
        Long commentId = 50L;

        User owner = User.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        User hacker = User.builder().build();
        ReflectionTestUtils.setField(hacker, "id", hackerId);

        Comment comment = Comment.builder().user(owner).content("원래 내용").build();

        given(userRepository.findById(hackerId)).willReturn(Optional.of(hacker));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        CommentUpdateRequest request = new CommentUpdateRequest("해킹 시도!");

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(hackerId, commentId, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 유저 ID로 요청하면 NotFoundException이 발생한다")
    void updateComment_Fail_UserNotFound() {
        // Given
        Long nonExistUserId = 1234L;
        given(userRepository.findById(nonExistUserId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(nonExistUserId, 1L, new CommentUpdateRequest("내용")))
                .isInstanceOf(NotFoundException.class);
    }
}