package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.exception.model.NotFoundException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.review.dto.CursorPageResponse;
import bangbang.gourmet.review.dto.FeedDetailCommentResponse;
import bangbang.gourmet.review.dto.FeedDetailResponse;
import bangbang.gourmet.review.dto.ReviewCountDto;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewFeedService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewFeedResponse> getFollowerFeed(Long userId, Long cursorId, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> reviewIds = reviewRepository.findFeedIdsByFollowerIdWithCursor(
                userId, cursorId, PageRequest.of(0, size + 1));

        boolean hasNext = reviewIds.size() > size;
        List<Long> pageIds = hasNext ? reviewIds.subList(0, size) : reviewIds;

        if (pageIds.isEmpty()) {
            return CursorPageResponse.of(List.of(), null, false);
        }

        Map<Long, Review> reviewMap = reviewRepository.findAllWithDetailsByIds(pageIds).stream()
                .collect(Collectors.toMap(Review::getId, r -> r));
        List<Review> reviews = pageIds.stream()
                .map(reviewMap::get)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Long> likeCountMap = reviewLikeRepository.countByReviewIds(pageIds).stream()
                .collect(Collectors.toMap(ReviewCountDto::reviewId, ReviewCountDto::count));

        Map<Long, Long> commentCountMap = commentRepository.countByReviewIds(pageIds).stream()
                .collect(Collectors.toMap(ReviewCountDto::reviewId, ReviewCountDto::count));

        Set<Long> likedReviewIds = Set.copyOf(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(userId, pageIds));

        List<ReviewFeedResponse> items = reviews.stream()
                .map(review -> {
                    long likeCount = likeCountMap.getOrDefault(review.getId(), 0L);
                    long commentCount = commentCountMap.getOrDefault(review.getId(), 0L);
                    boolean isLiked = likedReviewIds.contains(review.getId());
                    return ReviewFeedResponse.of(review, likeCount, commentCount, isLiked);
                })
                .toList();

        Long nextCursorId = hasNext ? pageIds.get(pageIds.size() - 1) : null;
        return CursorPageResponse.of(items, nextCursorId, hasNext);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewFeedResponse> getMyReviews(Long userId, boolean isPublic, Long cursorId, int size) {
        if (!userRepository.existsById(userId)) {
            throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> reviewIds = reviewRepository.findMyReviewIdsByUserIdWithCursor(
                userId, isPublic, cursorId, PageRequest.of(0, size + 1));

        boolean hasNext = reviewIds.size() > size;
        List<Long> pageIds = hasNext ? reviewIds.subList(0, size) : reviewIds;

        if (pageIds.isEmpty()) {
            return CursorPageResponse.of(List.of(), null, false);
        }

        Map<Long, Review> reviewMap = reviewRepository.findAllWithDetailsByIds(pageIds).stream()
                .collect(Collectors.toMap(Review::getId, r -> r));
        List<Review> reviews = pageIds.stream()
                .map(reviewMap::get)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Long> likeCountMap = reviewLikeRepository.countByReviewIds(pageIds).stream()
                .collect(Collectors.toMap(ReviewCountDto::reviewId, ReviewCountDto::count));

        Map<Long, Long> commentCountMap = commentRepository.countByReviewIds(pageIds).stream()
                .collect(Collectors.toMap(ReviewCountDto::reviewId, ReviewCountDto::count));

        Set<Long> likedReviewIds = Set.copyOf(reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(userId, pageIds));

        List<ReviewFeedResponse> items = reviews.stream()
                .map(review -> {
                    long likeCount = likeCountMap.getOrDefault(review.getId(), 0L);
                    long commentCount = commentCountMap.getOrDefault(review.getId(), 0L);
                    boolean isLiked = likedReviewIds.contains(review.getId());
                    return ReviewFeedResponse.of(review, likeCount, commentCount, isLiked);
                })
                .toList();

        Long nextCursorId = hasNext ? pageIds.get(pageIds.size() - 1) : null;
        return CursorPageResponse.of(items, nextCursorId, hasNext);
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