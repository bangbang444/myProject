package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewFeedServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewFeedService reviewFeedService;

    @Test
    @DisplayName("팔로우한 사용자의 리뷰만 피드에 노출된다")
    void getFollowerFeed_Success() {
        // given (준비)
        Long currentUserId = 1L;
        Long followingId = 2L;

        given(userRepository.existsById(currentUserId)).willReturn(true);

        // 1. 내가 2번 유저를 팔로우하고 있다고 가정
        given(followRepository.findFollowingIdsByFollowerId(currentUserId))
                .willReturn(List.of(followingId));

        // 2. 2번 유저가 쓴 가짜 리뷰 생성
        Review review = createMockReview(followingId);
        given(reviewRepository.findAllByUserIds(List.of(followingId)))
                .willReturn(List.of(review));

        // when (실행)
        List<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId);

        // then (검증)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(followingId);
        assertThat(result.get(0).restaurantName()).isEqualTo("준식이네 맛집");
    }

    @Test
    @DisplayName("팔로우하는 사람이 없으면 빈 피드가 반환된다")
    void getFollowerFeed_Empty() {
        // given
        Long currentUserId = 1L;
        given(userRepository.existsById(currentUserId)).willReturn(true);

        given(followRepository.findFollowingIdsByFollowerId(currentUserId))
                .willReturn(List.of());

        // when
        List<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId);

        // then
        assertThat(result).isEmpty();
        // Repository는 호출조차 되지 않아야 효율적입니다.
        verify(reviewRepository, never()).findAllByUserIds(any());
    }

    private Review createMockReview(Long authorId) {
        User author = User.builder().nickname("테스터").build();
        ReflectionTestUtils.setField(author, "id", authorId);

        Restaurant restaurant = Restaurant.builder()
                .restaurantName("준식이네 맛집")
                .mainCategory("한식")
                .build();
        ReflectionTestUtils.setField(restaurant, "restaurantId", 100L);

        Review review = Review.builder()
                .user(author)
                .restaurant(restaurant)
                .content("맛있어요!")
                .rating(4.5)
                .build();
        ReflectionTestUtils.setField(review, "id", 1000L);
        ReflectionTestUtils.setField(review, "createdDate", LocalDateTime.of(2024, 1, 1, 12, 0));
        ReflectionTestUtils.setField(review, "images", List.of()); // 이미지는 일단 빈 리스트

        return review;
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 피드 조회 시 USER_NOT_FOUND 예외가 발생한다")
    void getFollowerFeed_UserNotFound() {
        // given
        Long invalidUserId = 999L;
        // 유저가 존재하지 않는 상황 스터빙
        given(userRepository.existsById(invalidUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewFeedService.getFollowerFeed(invalidUserId))
                .isInstanceOf(BadRequestException.class);
    }

}