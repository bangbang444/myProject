package bangbang.gourmet.social.service;

import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.notification.service.NotificationService;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.dto.ReviewLikeResponse;
import bangbang.gourmet.social.entity.ReviewLike;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewLikeServiceTest {
    @Mock
    private ReviewLikeRepository reviewLikeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.builder().nickname("테스터").build();
        ReflectionTestUtils.setField(user, "id", 1L);

        review = Review.builder().content("맛있어요").build();
        ReflectionTestUtils.setField(review, "id", 100L);
    }

    @Test
    @DisplayName("좋아요가 없는 상태에서 누르면 좋아요가 생성되고 true를 반환한다")
    void toggleLike_Create() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reviewRepository.findById(100L)).willReturn(Optional.of(review));
        given(reviewLikeRepository.findByUserAndReview(user, review)).willReturn(Optional.empty());
        given(reviewLikeRepository.countByReview(review)).willReturn(1L);

        // when
        ReviewLikeResponse response = reviewLikeService.toggleLike(1L, 100L);

        // then
        assertThat(response.isLiked()).isTrue();
        assertThat(response.likeCount()).isEqualTo(1L);
        verify(reviewLikeRepository, times(1)).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("이미 좋아요가 있는 상태에서 누르면 좋아요가 삭제되고 false를 반환한다")
    void toggleLike_Delete() {
        // given
        ReviewLike existingLike = ReviewLike.builder().user(user).review(review).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reviewRepository.findById(100L)).willReturn(Optional.of(review));
        given(reviewLikeRepository.findByUserAndReview(user, review)).willReturn(Optional.of(existingLike));
        given(reviewLikeRepository.countByReview(review)).willReturn(0L);

        // when
        ReviewLikeResponse response = reviewLikeService.toggleLike(1L, 100L);

        // then
        assertThat(response.isLiked()).isFalse();
        assertThat(response.likeCount()).isEqualTo(0L);
        verify(reviewLikeRepository, times(1)).delete(existingLike);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 좋아요를 누르면 404 예외가 발생한다")
    void toggleLike_ReviewNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(reviewRepository.findById(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewLikeService.toggleLike(1L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 좋아요를 누르면 404 예외가 발생한다")
    void toggleLike_UserNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewLikeService.toggleLike(1L, 100L))
                .isInstanceOf(NotFoundException.class);
    }
}