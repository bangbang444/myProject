package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.ForbiddenException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;
import bangbang.gourmet.review.repository.ReviewImageRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static bangbang.gourmet.global.s3.S3Buckets.SNS;

@Service
@RequiredArgsConstructor
public class ReviewDbService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    @Transactional
    public Long saveReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<String> imageKeys) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.RESTAURANT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));

        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .tasteRating(request.tasteRating())
                .atmosphereRating(request.atmosphereRating())
                .serviceRating(request.serviceRating())
                .content(request.content())
                .category(request.category())
                .isPublic(request.isPublic())
                .build();

        Review savedReview = reviewRepository.save(review);

        imageKeys.forEach(savedReview::addReviewImage);
        if (!imageKeys.isEmpty()) {
            reviewImageRepository.saveAll(savedReview.getImages());
        }

        restaurant.addReview(review.getAverageRating());
        return savedReview.getId();
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    @Transactional
    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequest request, List<String> newImageKeys) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        Double oldAvgRating = review.getAverageRating();
        review.update(request.content(), request.tasteRating(), request.atmosphereRating(), request.serviceRating());
        Double newAvgRating = review.getAverageRating();
        if (!oldAvgRating.equals(newAvgRating)) {
            review.getRestaurant().updateReviewRating(oldAvgRating, newAvgRating);
        }

        if (request.deleteImageIds() != null && !request.deleteImageIds().isEmpty()) {
            List<ReviewImage> imagesToDelete = reviewImageRepository.findAllById(request.deleteImageIds());
            for (ReviewImage img : imagesToDelete) {
                if (!img.getReview().getId().equals(reviewId)) {
                    throw new BadRequestException(ErrorCode.INVALID_IMAGE_OWNER);
                }
            }
            imagesToDelete.forEach(image -> s3Service.delete(SNS, image.getImageUrl()));
            review.getImages().removeAll(imagesToDelete);
            reviewImageRepository.deleteAllInBatch(imagesToDelete);
        }

        newImageKeys.forEach(review::addReviewImage);
        if (!newImageKeys.isEmpty()) {
            reviewImageRepository.saveAll(review.getImages());
        }
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        review.getImages().forEach(image -> s3Service.delete(SNS, image.getImageUrl()));
        reviewImageRepository.deleteAllInBatch(review.getImages());
        reviewRepository.delete(review);
        review.getRestaurant().decreaseReviewCount(review.getAverageRating());
    }
}