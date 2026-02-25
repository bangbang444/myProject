package bangbang.gourmet.review.service;

import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
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
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, "정말 맛있어요!", List.of());

        // 가짜 이미지 파일
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        List<MultipartFile> images = List.of(image);

        // Mock 동작 정의
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurant));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(s3Service.upload(any(), anyString(), anyString())).willReturn("https://s3.url/test.jpg");
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when (실행)
        reviewService.createReview(restaurantId, userId, request, images);

        // then (검증)
        // 1. 평점 계산 검증: (4.0 * 1 + 5.0) / 2 = 4.5
        assertThat(restaurant.getReviewCount()).isEqualTo(2);
        assertThat(restaurant.getAverageRating()).isEqualTo(4.5);

        // 2. 저장 메서드 호출 횟수 검증
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewImageRepository, times(1)).save(any(ReviewImage.class));
        verify(s3Service, times(1)).upload(any(), eq("sns"), eq("reviews"));
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
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, "글만 있는 리뷰", List.of());
        List<MultipartFile> images = List.of(); // 빈 리스트

        // when
        reviewService.createReview(restaurantId, userId, request, images);

        // then
        verify(reviewImageRepository, times(0)).save(any());
        assertThat(restaurant.getReviewCount()).isEqualTo(1); // 0개에서 1개로 증가했는지도 확인!
    }
}