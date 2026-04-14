package bangbang.gourmet.review.service;

import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewResponse;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import bangbang.gourmet.review.dto.UserReviewStats;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;
import bangbang.gourmet.review.repository.ReviewImageRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private ReviewImageRepository reviewImageRepository;
    @Mock private UserRepository userRepository;
    @Mock private S3Service s3Service;

    @Test
    @DisplayName("리뷰를 등록하면 식당의 평점과 리뷰 개수가 갱신되어야 한다")
    void createReview_UpdatesRestaurantStats() {
        // given
        Long restaurantId = 1L;
        Long userId = 1L;

        // 기존 평점 4.0, 리뷰 개수 1개인 식당
        Restaurant restaurant = Restaurant.builder()
                .restaurantName("병현 맛집")
                .averageRating(4.0)
                .reviewCount(1)
                .build();

        User user = User.builder().nickname("jason").build();
        // 맛 5.0, 분위기 5.0, 서비스 5.0 -> 평균 5.0
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, 5.0, 5.0, "정말 맛있어요!", "한식", true);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        List<MultipartFile> images = List.of(image);

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("https://s3.url/test.jpg");
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reviewImageRepository.saveAll(anyList())).willReturn(List.of());

        // when
        reviewService.createReview(restaurantId, userId, request, images);

        // then
        // 평점 계산 검증: (4.0 * 1 + 5.0) / 2 = 4.5
        assertThat(restaurant.getReviewCount()).isEqualTo(2);
        assertThat(restaurant.getAverageRating()).isEqualTo(4.5);

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewImageRepository, times(1)).saveAll(anyList());
        verify(s3Service, times(1)).uploadImage(any(), eq("sns"), eq("reviews"));
    }

    @Test
    @DisplayName("이미지가 없는 리뷰를 등록해도 정상적으로 저장되어야 한다")
    void createReview_WithoutImages() {
        Long restaurantId = 1L;
        Long userId = 1L;
        Restaurant restaurant = Restaurant.builder().averageRating(0.0).reviewCount(0).build();
        User user = User.builder().nickname("jason").build();

        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

        ReviewCreateRequest request = new ReviewCreateRequest(5.0, 5.0, 5.0, "글만 있는 리뷰", "한식", true);
        List<MultipartFile> images = List.of();

        // when
        reviewService.createReview(restaurantId, userId, request, images);

        // then
        verify(reviewImageRepository, times(0)).saveAll(anyList());
        assertThat(restaurant.getReviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("식당 ID로 리뷰 목록을 조회하면 이미지 URL과 작성자 통계를 포함한 DTO 리스트가 반환되어야 한다")
    void getReviews_ReturnsReviewResponseList() {
        // given
        Long restaurantId = 1L;
        Long authorId = 10L;

        User user = User.builder().nickname("jason").build();
        ReflectionTestUtils.setField(user, "id", authorId);

        Restaurant restaurant = Restaurant.builder().build();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .tasteRating(4.0)
                .atmosphereRating(5.0)
                .serviceRating(4.5)
                .content("정말 맛있어요!")
                .category("한식")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(review, "images", new ArrayList<ReviewImage>());
        ReflectionTestUtils.setField(review, "id", 100L);

        review.addReviewImage("https://s3.url/image1.jpg");
        review.addReviewImage("https://s3.url/image2.jpg");

        for (int i = 0; i < review.getImages().size(); i++) {
            ReflectionTestUtils.setField(review.getImages().get(i), "id", (long) (i + 1));
        }

        List<Review> reviews = List.of(review);
        UserReviewStats authorStats = new UserReviewStats(authorId, 5L, 4.0);

        given(reviewRepository.findAllByRestaurantIdWithImages(restaurantId)).willReturn(reviews);
        given(reviewRepository.findUserReviewStatsByUserIds(List.of(authorId))).willReturn(List.of(authorStats));

        // when
        List<ReviewResponse> result = reviewService.getReviews(restaurantId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).reviewId()).isEqualTo(100L);
        assertThat(result.get(0).nickname()).isEqualTo("jason");
        assertThat(result.get(0).tasteRating()).isEqualTo(4.0);
        assertThat(result.get(0).atmosphereRating()).isEqualTo(5.0);
        assertThat(result.get(0).serviceRating()).isEqualTo(4.5);
        assertThat(result.get(0).category()).isEqualTo("한식");
        assertThat(result.get(0).isPublic()).isTrue();
        assertThat(result.get(0).authorReviewCount()).isEqualTo(5L);
        assertThat(result.get(0).authorAverageRating()).isEqualTo(4.0);

        List<String> extractedUrls = result.get(0).images().stream()
                .map(ReviewResponse.ReviewImageDetail::imageUrl)
                .toList();
        assertThat(extractedUrls).hasSize(2);
        assertThat(extractedUrls).contains(
                "https://s3.url/image1.jpg",
                "https://s3.url/image2.jpg"
        );
        assertThat(result.get(0).images().get(0).imageId()).isNotNull();

        verify(reviewRepository, times(1)).findAllByRestaurantIdWithImages(restaurantId);
        verify(reviewRepository, times(1)).findUserReviewStatsByUserIds(List.of(authorId));
    }

    @Test
    @DisplayName("리뷰 수정 성공 테스트 - 텍스트 수정 및 이미지 추가/삭제")
    void updateReview_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 100L;

        Restaurant restaurant = Restaurant.builder()
                .averageRating(3.0)
                .reviewCount(1)
                .build();

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .content("원래 내용")
                .tasteRating(3.0)
                .atmosphereRating(3.0)
                .serviceRating(3.0)
                .category("한식")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        ReviewImage img1 = ReviewImage.builder()
                .imageUrl("old-image-url.jpg")
                .review(review)
                .build();
        ReflectionTestUtils.setField(img1, "id", 10L);
        review.getImages().add(img1);

        // 맛 5.0, 분위기 5.0, 서비스 5.0 -> 평균 5.0
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 5.0, 5.0, 5.0, List.of(10L));

        MockMultipartFile newImage = new MockMultipartFile("newImages", "new.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> newImages = List.of(newImage);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewImageRepository.findAllById(anyList())).willReturn(List.of(img1));
        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("new-key.jpg");

        // when
        reviewService.updateReview(userId, reviewId, request, newImages);

        // then
        assertThat(review.getContent()).isEqualTo("수정된 내용");
        assertThat(review.getTasteRating()).isEqualTo(5.0);
        assertThat(review.getAtmosphereRating()).isEqualTo(5.0);
        assertThat(review.getServiceRating()).isEqualTo(5.0);

        verify(s3Service, times(1)).delete(anyString(), anyString());
        verify(reviewImageRepository, times(1)).deleteAllInBatch(anyList());
        verify(reviewImageRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 수정 시 식당의 평균 평점이 갱신되는지 확인한다")
    void updateReview_UpdateRestaurantRating() {
        // given
        Long userId = 1L;

        // 기존 평점 3.0 (맛 3.0, 분위기 3.0, 서비스 3.0 -> 평균 3.0), 리뷰 1개
        Restaurant restaurant = Restaurant.builder()
                .averageRating(3.0)
                .reviewCount(1)
                .build();

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .tasteRating(3.0)
                .atmosphereRating(3.0)
                .serviceRating(3.0)
                .build();

        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));

        // 맛 5.0, 분위기 5.0, 서비스 5.0 -> 평균 5.0으로 수정
        ReviewUpdateRequest request = new ReviewUpdateRequest("내용", 5.0, 5.0, 5.0, null);

        // when
        reviewService.updateReview(userId, 1L, request, null);

        // then
        // (3.0 * 1 - 3.0 + 5.0) / 1 = 5.0
        assertThat(restaurant.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("리뷰 삭제 성공 테스트 - 식당 통계 반영 및 이미지/리뷰 삭제")
    void deleteReview_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 100L;

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        // 식당 리뷰 2개, 평점 4.5 가정
        Restaurant restaurant = Restaurant.builder()
                .averageRating(4.5)
                .reviewCount(2)
                .build();

        // 삭제할 리뷰: 맛 5.0, 분위기 5.0, 서비스 5.0 -> 평균 5.0
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .tasteRating(5.0)
                .atmosphereRating(5.0)
                .serviceRating(5.0)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        ReviewImage img1 = ReviewImage.builder().imageUrl("delete-me.jpg").review(review).build();
        review.getImages().add(img1);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        reviewService.deleteReview(userId, reviewId);

        // then
        // (4.5 * 2 - 5.0) / 1 = 4.0
        assertThat(restaurant.getReviewCount()).isEqualTo(1);
        assertThat(restaurant.getAverageRating()).isEqualTo(4.0);

        verify(s3Service, times(1)).delete(anyString(), eq("delete-me.jpg"));
        verify(reviewImageRepository, times(1)).deleteAllInBatch(anyList());
        verify(reviewRepository, times(1)).delete(any(Review.class));
    }
}
