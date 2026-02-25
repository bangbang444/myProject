package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.response.ErrorCode;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static bangbang.gourmet.global.s3.S3Buckets.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public Long createReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<MultipartFile> images) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.RESTAURANT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        // 1. 리뷰 엔티티 생성 및 저장
        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .rating(request.rating())
                .content(request.content())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 2. 이미지 업로드
        if(images != null && !images.isEmpty()){
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = s3Service.upload(image, SNS, REVIEWS); // 병현님의 업로드 로직 호출

                    // Setter 대신 생성자로 연관관계 맺기
                    ReviewImage reviewImage = new ReviewImage(savedReview, imageUrl);
                    reviewImageRepository.save(reviewImage);
                }
            }
        }

        // 3. 식당 평점 및 리뷰개수 동기화
        restaurant.addReview(request.rating());

        return savedReview.getId();
    }
}
