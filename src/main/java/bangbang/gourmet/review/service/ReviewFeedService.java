package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.review.dto.FeedDetailCommentResponse;
import bangbang.gourmet.review.dto.FeedDetailResponse;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewFeedService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional(readOnly = true)
    public List<ReviewFeedResponse> getFollowerFeed(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. 내가 팔로우하는 유저들의 ID 목록 조회
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(userId);

        // 2. 만약 팔로우하는 사람이 없다면 빈 리스트 반환 (혹은 추천 피드)
        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<Review> reviews = reviewRepository.findAllByUserIds(followingIds);


        // TODO: 3번 쿼리(n+1) 문제, 리뷰 목록으로 한번에 조회하기
        return reviews.stream()
                .map(review -> {
                    long likeCount = reviewLikeRepository.countByReview(review);
                    long commentCount = commentRepository.countByReview(review);
                    boolean isLiked = reviewLikeRepository.existsByUserIdAndReviewId(userId, review.getId());
                    return ReviewFeedResponse.of(review, likeCount, commentCount, isLiked);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedDetailResponse getFeedDetail(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        long likeCount = reviewLikeRepository.countByReview(review);
        boolean isLiked = reviewLikeRepository.existsByUserIdAndReviewId(userId, review.getId());

        List<Comment> comments = commentRepository.findAllByReviewWithUserOrderByCreatedDate(review);
        List<FeedDetailCommentResponse> commentResponses = comments.stream()
                .map(FeedDetailCommentResponse::from)
                .collect(Collectors.toList());

        return FeedDetailResponse.of(review, likeCount, isLiked, commentResponses);
    }
}
