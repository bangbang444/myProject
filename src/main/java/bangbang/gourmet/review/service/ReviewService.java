package bangbang.gourmet.review.service;

import bangbang.gourmet.common.util.RatingUtils;
import bangbang.gourmet.global.s3.S3Service;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewResponse;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import bangbang.gourmet.review.dto.UserReviewStats;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.ReviewRepository;
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
    private final S3Service s3Service;
    private final ReviewDbService reviewDbService;

    public Long createReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<MultipartFile> images) {
        // TODO: DB 최종 실패 시 고아 S3 파일 정리 배치 필요 (S3 키 목록 vs DB 비교)
        List<String> imageKeys = uploadImages(images);
        return reviewDbService.saveReview(restaurantId, userId, request, imageKeys);
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

    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequest request, List<MultipartFile> newImages) {
        List<String> newImageKeys = uploadImages(newImages);
        reviewDbService.updateReview(userId, reviewId, request, newImageKeys);
    }

    public void deleteReview(Long userId, Long reviewId) {
        reviewDbService.deleteReview(userId, reviewId);
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return List.of();
        return images.stream()
                .filter(image -> !image.isEmpty())
                .map(image -> s3Service.uploadImage(image, SNS, REVIEWS))
                .toList();
    }
}