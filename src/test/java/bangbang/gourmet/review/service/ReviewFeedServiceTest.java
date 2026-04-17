package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.review.dto.CursorPageResponse;
import bangbang.gourmet.review.dto.ReviewCountDto;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReviewFeedServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @InjectMocks
    private ReviewFeedService reviewFeedService;

    @Test
    @DisplayName("팔로우한 사용자의 리뷰만 피드에 노출된다")
    void getFollowerFeed_Success() {
        // given
        Long currentUserId = 1L;
        Long followingId = 2L;

        given(userRepository.existsById(currentUserId)).willReturn(true);

        Review review = createMockReview(followingId);
        given(reviewRepository.findFeedIdsByFollowerIdWithCursor(eq(currentUserId), isNull(), any(Pageable.class)))
                .willReturn(List.of(review.getId()));
        given(reviewRepository.findAllWithDetailsByIds(anyList()))
                .willReturn(List.of(review));

        given(reviewLikeRepository.countByReviewIds(anyList()))
                .willReturn(List.of(new ReviewCountDto(review.getId(), 5L)));
        given(commentRepository.countByReviewIds(anyList()))
                .willReturn(List.of(new ReviewCountDto(review.getId(), 3L)));
        given(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(eq(currentUserId), anyList()))
                .willReturn(List.of(review.getId()));

        // when
        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId, null, 20);

        // then
        assertThat(result.items()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursorId()).isNull();
        assertThat(result.items().get(0).userId()).isEqualTo(followingId);
        assertThat(result.items().get(0).restaurantName()).isEqualTo("준식이네 맛집");
        assertThat(result.items().get(0).likeCount()).isEqualTo(5L);
        assertThat(result.items().get(0).commentCount()).isEqualTo(3L);
        assertThat(result.items().get(0).isLiked()).isTrue();
    }

    @Test
    @DisplayName("팔로우하는 사람이 없으면 빈 피드가 반환된다")
    void getFollowerFeed_Empty() {
        // given
        Long currentUserId = 1L;
        given(userRepository.existsById(currentUserId)).willReturn(true);
        given(reviewRepository.findFeedIdsByFollowerIdWithCursor(eq(currentUserId), isNull(), any(Pageable.class)))
                .willReturn(List.of());

        // when
        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId, null, 20);

        // then
        assertThat(result.items()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("isPublic=false인 리뷰는 피드에 노출되지 않는다")
    void getFollowerFeed_ExcludesPrivateReviews() {
        // given
        Long currentUserId = 1L;

        given(userRepository.existsById(currentUserId)).willReturn(true);
        given(reviewRepository.findFeedIdsByFollowerIdWithCursor(eq(currentUserId), isNull(), any(Pageable.class)))
                .willReturn(List.of());

        // when
        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId, null, 20);

        // then
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("다음 페이지가 있으면 hasNext=true이고 nextCursorId가 반환된다")
    void getFollowerFeed_HasNext() {
        // given
        Long currentUserId = 1L;
        int size = 2;

        given(userRepository.existsById(currentUserId)).willReturn(true);

        Review review1 = createMockReviewWithId(2L, 1001L);
        Review review2 = createMockReviewWithId(2L, 1000L);
        Review extraReview = createMockReviewWithId(2L, 999L);

        // size+1 개 반환 → hasNext=true
        given(reviewRepository.findFeedIdsByFollowerIdWithCursor(eq(currentUserId), isNull(), any(Pageable.class)))
                .willReturn(List.of(1001L, 1000L, 999L));
        given(reviewRepository.findAllWithDetailsByIds(List.of(1001L, 1000L)))
                .willReturn(List.of(review1, review2));

        given(reviewLikeRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(commentRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(any(), anyList())).willReturn(List.of());

        // when
        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getFollowerFeed(currentUserId, null, size);

        // then
        assertThat(result.items()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursorId()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("내 리뷰 목록을 isPublic 조건으로 조회한다")
    void getMyReviews_Success() {
        Long userId = 1L;
        Review review = createMockReview(userId);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findMyReviewIdsByUserIdWithCursor(eq(userId), eq(true), isNull(), any(Pageable.class)))
                .willReturn(List.of(review.getId()));
        given(reviewRepository.findAllWithDetailsByIds(anyList())).willReturn(List.of(review));
        given(reviewLikeRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(commentRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(eq(userId), anyList())).willReturn(List.of());

        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getMyReviews(userId, true, null, 12);

        assertThat(result.items()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.items().get(0).userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("내 리뷰가 없으면 빈 목록이 반환된다")
    void getMyReviews_Empty() {
        Long userId = 1L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findMyReviewIdsByUserIdWithCursor(eq(userId), eq(false), isNull(), any(Pageable.class)))
                .willReturn(List.of());

        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getMyReviews(userId, false, null, 12);

        assertThat(result.items()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("내 리뷰 조회 시 다음 페이지가 있으면 hasNext=true이고 nextCursorId가 반환된다")
    void getMyReviews_HasNext() {
        Long userId = 1L;
        int size = 2;

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findMyReviewIdsByUserIdWithCursor(eq(userId), eq(true), isNull(), any(Pageable.class)))
                .willReturn(List.of(1001L, 1000L, 999L));
        given(reviewRepository.findAllWithDetailsByIds(List.of(1001L, 1000L)))
                .willReturn(List.of(createMockReviewWithId(userId, 1001L), createMockReviewWithId(userId, 1000L)));
        given(reviewLikeRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(commentRepository.countByReviewIds(anyList())).willReturn(List.of());
        given(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(any(), anyList())).willReturn(List.of());

        CursorPageResponse<ReviewFeedResponse> result = reviewFeedService.getMyReviews(userId, true, null, size);

        assertThat(result.items()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursorId()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 피드 조회 시 USER_NOT_FOUND 예외가 발생한다")
    void getFollowerFeed_UserNotFound() {
        // given
        Long invalidUserId = 999L;
        given(userRepository.existsById(invalidUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewFeedService.getFollowerFeed(invalidUserId, null, 20))
                .isInstanceOf(BadRequestException.class);
    }

    private Review createMockReview(Long authorId) {
        return createMockReviewWithId(authorId, 1000L);
    }

    private Review createMockReviewWithId(Long authorId, Long reviewId) {
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
                .tasteRating(4.5)
                .atmosphereRating(4.5)
                .serviceRating(4.5)
                .category("한식")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);
        ReflectionTestUtils.setField(review, "createdDate", LocalDateTime.of(2024, 1, 1, 12, 0));
        ReflectionTestUtils.setField(review, "images", List.of());

        return review;
    }
}
