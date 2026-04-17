package bangbang.gourmet.review.service;

import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.dto.ReviewUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewRetryService {

    private final ReviewDbService reviewDbService;

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    public Long saveReview(Long restaurantId, Long userId, ReviewCreateRequest request, List<String> imageKeys) {
        return reviewDbService.saveReview(restaurantId, userId, request, imageKeys);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequest request, List<String> newImageKeys) {
        reviewDbService.updateReview(userId, reviewId, request, newImageKeys);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 300, random = true))
    public void deleteReview(Long userId, Long reviewId) {
        reviewDbService.deleteReview(userId, reviewId);
    }
}