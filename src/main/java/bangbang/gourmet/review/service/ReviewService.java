package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.ForbiddenException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.common.util.RatingUtils;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static bangbang.gourmet.global.s3.S3Buckets.REVIEWS;
import static bangbang.gourmet.global.s3.S3Buckets.SNS;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public Long createReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<MultipartFile> images) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.RESTAURANT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        // 1. 리뷰 엔티티 생성 및 저장
        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .tasteRating(request.tasteRating())
                .atmosphereRating(request.atmosphereRating())
                .serviceRating(request.serviceRating())
                .content(request.content())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 2. 이미지 업로드
        if (images != null && !images.isEmpty()) {
            images.stream()
                    .filter(image -> !image.isEmpty())
                    .forEach(image -> {
                        String uploadKey = s3Service.uploadImage(image, SNS, REVIEWS);
                        savedReview.addReviewImage(uploadKey);
                    });

            reviewImageRepository.saveAll(savedReview.getImages());
        }

        // 3. 식당 평점 및 리뷰개수 동기화
        restaurant.addReview(review.getAverageRating());

        return savedReview.getId();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long restaurantId) {
        List<Review> reviews = reviewRepository.findAllByRestaurantIdWithImages(restaurantId);

        List<Long> userIds = reviews.stream()
                .map(r -> r.getUser().getId())
                .distinct()
                .toList();

        Map<Long, UserReviewStats> statsMap = reviewRepository.findUserReviewStatsByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserReviewStats::userId, s -> s));

        return reviews.stream()
                .map(review -> {
                    UserReviewStats stats = Optional.ofNullable(statsMap.get(review.getUser().getId()))
                            .orElseThrow(() -> new IllegalStateException(
                                    "리뷰가 존재하는 사용자의 통계가 없습니다. userId=" + review.getUser().getId()
                            ));
                    return ReviewResponse.of(review, stats.reviewCount(), RatingUtils.roundToOneDecimal(stats.averageRating()));
                })
                .toList();
    }

    @Transactional
    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages) {
        // 1. 리뷰 조회 및 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        // 2. 평점 및 식당 통계 업데이트
        Double oldAvgRating = review.getAverageRating();
        review.update(request.content(), request.tasteRating(), request.atmosphereRating(), request.serviceRating());
        Double newAvgRating = review.getAverageRating();
        if (!oldAvgRating.equals(newAvgRating)) {
            review.getRestaurant().updateReviewRating(oldAvgRating, newAvgRating);
        }

        // 3. 이미지 삭제 처리 (직접 삭제)
        if (request.deleteImageIds() != null && !request.deleteImageIds().isEmpty()) {
            // 보안: 해당 리뷰에 속한 이미지만 필터링해서 조회
            List<ReviewImage> imagesToDelete = reviewImageRepository.findAllById(request.deleteImageIds());
            for (ReviewImage img : imagesToDelete) {
                if (!img.getReview().getId().equals(reviewId)) {
                    throw new BadRequestException(ErrorCode.INVALID_IMAGE_OWNER);
                }
            }

            // S3에서 파일 삭제 및 DB 레코드 삭제
            imagesToDelete.forEach(image -> s3Service.delete(SNS, image.getImageUrl()));
            review.getImages().removeAll(imagesToDelete);
            reviewImageRepository.deleteAllInBatch(imagesToDelete);
        }

        // 4. 새 이미지 추가 처리
        if (newImages != null && !newImages.isEmpty()) {
            newImages.forEach(image -> {
                String imageUrl = s3Service.uploadImage(image, SNS, REVIEWS);
                review.addReviewImage(imageUrl);
            });
            reviewImageRepository.saveAll(review.getImages());
        }
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. 권한 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        // 3. S3에서 이 리뷰에 달린 모든 이미지 삭제
        review.getImages().forEach(image -> s3Service.delete(SNS, image.getImageUrl()));

        // 4. DB에서 리뷰 삭제 - 이미지 레코드부터 지우는 게 안전
        reviewImageRepository.deleteAllInBatch(review.getImages());
        reviewRepository.delete(review);

        // 5. 식당 평점/리뷰 개수 갱신
        review.getRestaurant().decreaseReviewCount(review.getAverageRating());
    }
}
