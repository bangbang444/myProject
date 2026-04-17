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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    @Transactional
    public Long saveReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<String> imageKeys) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESTAURANT_NOT_FOUND));

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
        if (Double.compare(oldAvgRating, newAvgRating) != 0) {
            review.getRestaurant().updateReviewRating(oldAvgRating, newAvgRating);
        }

        if (request.deleteImageIds() != null && !request.deleteImageIds().isEmpty()) {
            List<ReviewImage> imagesToDelete = reviewImageRepository.findAllById(request.deleteImageIds());
            for (ReviewImage img : imagesToDelete) {
                if (!img.getReview().getId().equals(reviewId)) {
                    throw new BadRequestException(ErrorCode.INVALID_IMAGE_OWNER);
                }
            }
            List<String> keysToDelete = imagesToDelete.stream()
                    .map(ReviewImage::getImageUrl).toList();
            review.getImages().removeAll(imagesToDelete);
            reviewImageRepository.deleteAllInBatch(imagesToDelete);
            registerS3DeleteAfterCommit(keysToDelete);
        }

        newImageKeys.forEach(review::addReviewImage);
        if (!newImageKeys.isEmpty()) {
            reviewImageRepository.saveAll(review.getImages());
        }
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new ForbiddenException(ErrorCode.NOT_OWNER_ERROR);
        }

        Restaurant restaurant = review.getRestaurant();
        double avgRating = review.getAverageRating();
        List<String> keysToDelete = review.getImages().stream()
                .map(ReviewImage::getImageUrl).toList();

        reviewImageRepository.deleteAllInBatch(review.getImages());
        reviewRepository.delete(review);
        restaurant.decreaseReviewCount(avgRating);

        registerS3DeleteAfterCommit(keysToDelete);
    }

    private void registerS3DeleteAfterCommit(List<String> keys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                keys.forEach(key -> s3Service.delete(SNS, key));
            }
        });
    }
}