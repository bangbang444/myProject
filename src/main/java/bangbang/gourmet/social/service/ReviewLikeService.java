package bangbang.gourmet.social.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.dto.ReviewLikeResponse;
import bangbang.gourmet.social.entity.ReviewLike;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewLikeResponse toggleLike(Long userId, Long reviewId) {
        // 1. 유저와 리뷰가 존재하는지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. 이미 좋아요를 눌렀는지 확인
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserAndReview(user, review);

        boolean isLiked;
        if (existingLike.isPresent()) {
            reviewLikeRepository.delete(existingLike.get());
            isLiked = false;
        } else {
            reviewLikeRepository.save(ReviewLike.builder().user(user).review(review).build());
            isLiked = true;
        }

        // 3. 최신 좋아요 수 조회 및 DTO 반환
        long likeCount = reviewLikeRepository.countByReview(review);

        return ReviewLikeResponse.of(isLiked, likeCount);
    }
}
