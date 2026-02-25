package bangbang.gourmet.review.service;

import bangbang.gourmet.common.exception.model.BadRequestException;
import bangbang.gourmet.common.response.ErrorCode;
import bangbang.gourmet.review.dto.ReviewFeedResponse;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.entity.ReviewImage;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewFeedService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public List<ReviewFeedResponse> getFollowerFeed(Long currentUserId) {
        if (!userRepository.existsById(currentUserId)) {
            throw new BadRequestException(ErrorCode.USER_NOT_FOUND);
        }

        // 1. 내가 팔로우하는 유저들의 ID 목록 조회
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(currentUserId);

        // 2. 만약 팔로우하는 사람이 없다면 빈 리스트 반환 (혹은 추천 피드)
        if (followingIds.isEmpty()) {
            return List.of();
        }

        // 3. 해당 유저들의 리뷰만 Fetch Join으로 조회
        return reviewRepository.findAllByUserIds(followingIds).stream()
                .map(ReviewFeedResponse::from)
                .toList();
    }
}
