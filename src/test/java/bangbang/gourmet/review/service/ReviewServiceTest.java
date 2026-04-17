package bangbang.gourmet.review.service;

import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewResponse;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import bangbang.gourmet.review.dto.UserReviewStats;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.user.entity.User;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private S3Service s3Service;
    @Mock private ReviewRetryService reviewRetryService;

    @Test
    @DisplayName("이미지가 있는 리뷰 작성 시 S3 업로드 후 DB 저장을 위임한다")
    void createReview_UploadsImagesBeforeDbSave() {
        Long restaurantId = 1L;
        Long userId = 1L;
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, 5.0, 5.0, "정말 맛있어요!", "한식", true);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        List<MultipartFile> images = List.of(image);

        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("uploaded-key.jpg");
        given(reviewRetryService.saveReview(eq(restaurantId), eq(userId), eq(request), anyList())).willReturn(100L);

        Long result = reviewService.createReview(restaurantId, userId, request, images);

        assertThat(result).isEqualTo(100L);
        verify(s3Service, times(1)).uploadImage(any(), eq("sns"), eq("reviews"));
        verify(reviewRetryService, times(1)).saveReview(eq(restaurantId), eq(userId), eq(request), eq(List.of("uploaded-key.jpg")));
    }

    @Test
    @DisplayName("이미지가 없는 리뷰 작성 시 S3 업로드 없이 DB 저장을 위임한다")
    void createReview_WithoutImages() {
        Long restaurantId = 1L;
        Long userId = 1L;
        ReviewCreateRequest request = new ReviewCreateRequest(5.0, 5.0, 5.0, "글만 있는 리뷰", "한식", true);

        given(reviewRetryService.saveReview(eq(restaurantId), eq(userId), eq(request), eq(List.of()))).willReturn(100L);

        reviewService.createReview(restaurantId, userId, request, List.of());

        verify(s3Service, never()).uploadImage(any(), anyString(), anyString());
        verify(reviewRetryService, times(1)).saveReview(eq(restaurantId), eq(userId), eq(request), eq(List.of()));
    }

    @Test
    @DisplayName("리뷰 수정 시 새 이미지를 S3 업로드 후 DB 수정을 위임한다")
    void updateReview_UploadsNewImagesBeforeDbUpdate() {
        Long userId = 1L;
        Long reviewId = 100L;
        ReviewUpdateRequest request = new ReviewUpdateRequest("수정된 내용", 5.0, 5.0, 5.0, List.of(10L));
        MockMultipartFile newImage = new MockMultipartFile("images", "new.jpg", "image/jpeg", "content".getBytes());

        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("new-key.jpg");

        reviewService.updateReview(userId, reviewId, request, List.of(newImage));

        verify(s3Service, times(1)).uploadImage(any(), anyString(), anyString());
        verify(reviewRetryService, times(1)).updateReview(eq(userId), eq(reviewId), eq(request), eq(List.of("new-key.jpg")));
    }

    @Test
    @DisplayName("리뷰 삭제 시 DB 삭제를 위임한다")
    void deleteReview_DelegatesToDbService() {
        reviewService.deleteReview(1L, 100L);
        verify(reviewRetryService, times(1)).deleteReview(1L, 100L);
    }

    @Test
    @DisplayName("식당 ID로 리뷰 목록을 조회하면 이미지 URL과 작성자 통계를 포함한 DTO 리스트가 반환되어야 한다")
    void getReviews_ReturnsReviewResponseList() {
        Long restaurantId = 1L;
        Long authorId = 10L;

        User user = User.builder().nickname("jason").build();
        ReflectionTestUtils.setField(user, "id", authorId);

        Restaurant restaurant = Restaurant.builder().build();
        ReflectionTestUtils.setField(restaurant, "restaurantId", restaurantId);

        Review review = Review.builder()
                .user(user).restaurant(restaurant)
                .tasteRating(4.0).atmosphereRating(5.0).serviceRating(4.5)
                .content("정말 맛있어요!").category("한식").isPublic(true)
                .build();
        ReflectionTestUtils.setField(review, "images", new ArrayList<ReviewImage>());
        ReflectionTestUtils.setField(review, "id", 100L);

        review.addReviewImage("https://s3.url/image1.jpg");
        review.addReviewImage("https://s3.url/image2.jpg");
        for (int i = 0; i < review.getImages().size(); i++) {
            ReflectionTestUtils.setField(review.getImages().get(i), "id", (long) (i + 1));
        }

        UserReviewStats authorStats = new UserReviewStats(authorId, 5L, 4.0);

        given(reviewRepository.findAllByRestaurantIdWithImages(restaurantId)).willReturn(List.of(review));
        given(reviewRepository.findUserReviewStatsByUserIds(List.of(authorId))).willReturn(List.of(authorStats));

        List<ReviewResponse> result = reviewService.getReviews(restaurantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).reviewId()).isEqualTo(100L);
        assertThat(result.get(0).nickname()).isEqualTo("jason");
        assertThat(result.get(0).images()).hasSize(2);
    }
}