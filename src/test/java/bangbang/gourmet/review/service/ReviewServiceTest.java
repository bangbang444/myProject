package bangbang.gourmet.review.service;

import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewResponse;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
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
        // given (준비)
        Long restaurantId = 1L;
        Long userId = 1L;

        // 기존 평점 4.0, 리뷰 개수 1개인 식당
        Restaurant restaurant = Restaurant.builder()
                .restaurantName("병현 맛집")
                .averageRating(4.0)
                .reviewCount(1)
                .build();

        User user = User.builder().nickname("jason").build();
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, "정말 맛있어요!");

        // 가짜 이미지 파일
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        List<MultipartFile> images = List.of(image);

        // Mock 동작 정의
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("https://s3.url/test.jpg");

        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(reviewImageRepository.saveAll(anyList())).willReturn(List.of());

        // when (실행)
        reviewService.createReview(restaurantId, userId, request, images);

        // then (검증)
        // 1. 평점 계산 검증: (4.0 * 1 + 5.0) / 2 = 4.5
        assertThat(restaurant.getReviewCount()).isEqualTo(2);
        assertThat(restaurant.getAverageRating()).isEqualTo(4.5);

        // 2. 저장 메서드 호출 횟수 검증
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewImageRepository, times(1)).saveAll(anyList());
        verify(s3Service, times(1)).uploadImage(any(), eq("sns"), eq("reviews"));
    }

    @Test
    @DisplayName("이미지가 없는 리뷰를 등록해도 정상적으로 저장되어야 한다")
    void createReview_WithoutImages() {
        // 💡 1. ID 및 가짜 엔티티 준비
        Long restaurantId = 1L;
        Long userId = 1L;
        Restaurant restaurant = Restaurant.builder().averageRating(0.0).reviewCount(0).build();
        User user = User.builder().nickname("jason").build();

        // 💡 2. Mock 동작 정의 (이게 있어야 Service 안의 findById가 null을 안 뱉음)
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

        // given
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, "글만 있는 리뷰");
        List<MultipartFile> images = List.of(); // 빈 리스트

        // when
        reviewService.createReview(restaurantId, userId, request, images);

        // then
        verify(reviewImageRepository, times(0)).saveAll(anyList());
        assertThat(restaurant.getReviewCount()).isEqualTo(1); // 0개에서 1개로 증가했는지도 확인!
    }

    @Test
    @DisplayName("식당 ID로 리뷰 목록을 조회하면 이미지 URL을 포함한 DTO 리스트가 반환되어야 한다")
    void getReviews_ReturnsReviewResponseList() {
        // given (준비)
        Long restaurantId = 1L;

        // 1. 가짜 유저 및 식당 생성
        User user = User.builder().nickname("jason").build();
        Restaurant restaurant = Restaurant.builder().build();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId); // 식당 ID 주입

        // 2. 가짜 리뷰 생성 및 ID 주입
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .rating(4.5)
                .content("정말 맛있어요!")
                .build();
        ReflectionTestUtils.setField(review, "id", 100L); // 리뷰 ID 주입

        // 3. 리뷰 이미지 생성 및 연결
        review.addReviewImage("https://s3.url/image1.jpg");
        review.addReviewImage("https://s3.url/image2.jpg");

        List<Review> reviews = List.of(review);

        // Mock 동작 정의
        given(reviewRepository.findAllByRestaurantIdWithImages(restaurantId)).willReturn(reviews);

        // when (실행)
        List<ReviewResponse> result = reviewService.getReviews(restaurantId);

        // then (검증)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).reviewId()).isEqualTo(100L); // ID 검증 추가
        assertThat(result.get(0).nickname()).isEqualTo("jason");
        assertThat(result.get(0).rating()).isEqualTo(4.5);

        List<String> extractedUrls = result.get(0).images().stream()
                .map(ReviewResponse.ReviewImageDetail::imageUrl)
                .toList();
        assertThat(extractedUrls).hasSize(2);
        assertThat(extractedUrls).contains(
                "https://s3.url/image1.jpg",
                "https://s3.url/image2.jpg"
        );
        assertThat(result.get(0).images().get(0).imageId()).isNotNull();

        // Repository 메서드가 호출되었는지 확인
        verify(reviewRepository, times(1)).findAllByRestaurantIdWithImages(restaurantId);
    }

    @Test
    @DisplayName("리뷰 수정 성공 테스트 - 텍스트 수정 및 이미지 추가/삭제")
    void updateReview_Success() {
        // 💡 1. 준비 (Given)
        Long userId = 1L;
        Long reviewId = 100L;

        // 가짜 유저와 기존 리뷰 생성
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        Review review = Review.builder()
                .user(user)
                .content("원래 내용")
                .rating(3.0)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        // 삭제할 가짜 이미지들 생성 (ID 10)
        ReviewImage img1 = ReviewImage.builder()
                .imageUrl("old-image-url.jpg")
                .review(review)
                .build();
        ReflectionTestUtils.setField(img1, "id", 10L);
        review.getImages().add(img1);

        // 리뷰의 이미지 리스트에 추가 (양방향 연결)
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 5.0, List.of(10L));

        MockMultipartFile newImage = new MockMultipartFile("newImages", "new.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> newImages = List.of(newImage);

        // Mock 설정
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewImageRepository.findAllById(anyList())).willReturn(List.of(img1));
        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("new-key.jpg");

        // 💡 2. 실행 (When)
        reviewService.updateReview(userId, reviewId, request, newImages);

        // 💡 3. 검증 (Then)
        // 텍스트 및 평점 수정 확인 (더티 체킹)
        assertThat(review.getContent()).isEqualTo("수정된 내용");
        assertThat(review.getRating()).isEqualTo(5.0);

        verify(s3Service, times(1)).delete(anyString(), anyString()); // S3 삭제 호출 확인
        verify(reviewImageRepository, times(1)).deleteAllInBatch(anyList()); // DB 삭제 호출 확인
        verify(reviewImageRepository, times(1)).saveAll(anyList()); // 새 이미지 저장 확인
    }

    @Test
    @DisplayName("리뷰 수정 시 식당의 평균 평점이 갱신되는지 확인한다")
    void updateReview_UpdateRestaurantRating() {
        // Given
        Long userId = 1L;

        // 식당 생성 (기존 평점 3.0, 리뷰 개수 1개라고 가정)
        Restaurant restaurant = Restaurant.builder()
                .averageRating(3.0)
                .reviewCount(1)
                .build();

        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        // 기존 리뷰 (평점 3.0)
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .rating(3.0)
                .build();

        given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));

        // 평점을 5.0으로 수정하는 요청
        ReviewUpdateRequest request = new ReviewUpdateRequest("내용", 5.0, null);

        // When
        reviewService.updateReview(userId, 1L, request, null);

        // Then
        // (3.0 * 1 - 3.0 + 5.0) / 1 = 5.0
        assertThat(restaurant.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("리뷰 삭제 성공 테스트 - 식당 통계 반영 및 이미지/리뷰 삭제")
    void deleteReview_Success() {
        // 💡 1. 준비 (Given)
        Long userId = 1L;
        Long reviewId = 100L;

        // 가짜 유저 생성
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", userId);

        // 가짜 식당 생성 (리뷰 2개, 평점 4.5점 가정)
        Restaurant restaurant = Restaurant.builder()
                .averageRating(4.5)
                .reviewCount(2)
                .build();

        // 삭제할 리뷰 생성 (평점 5.0점)
        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .rating(5.0)
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        // 삭제될 이미지 하나 추가
        ReviewImage img1 = ReviewImage.builder().imageUrl("delete-me.jpg").review(review).build();
        review.getImages().add(img1);

        // Mock 동작 정의
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // 💡 2. 실행 (When)
        reviewService.deleteReview(userId, reviewId);

        // 💡 3. 검증 (Then)
        // 식당 통계 검증: (4.5 * 2 - 5.0) / 1 = 4.0
        assertThat(restaurant.getReviewCount()).isEqualTo(1);
        assertThat(restaurant.getAverageRating()).isEqualTo(4.0);

        // S3 삭제 호출 확인
        verify(s3Service, times(1)).delete(anyString(), eq("delete-me.jpg"));

        // DB 삭제 호출 확인
        verify(reviewImageRepository, times(1)).deleteAllInBatch(anyList());
        verify(reviewRepository, times(1)).delete(any(Review.class));
    }
}