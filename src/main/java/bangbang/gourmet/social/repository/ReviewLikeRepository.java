package bangbang.gourmet.social.repository;

import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.social.entity.ReviewLike;
import bangbang.gourmet.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    // 토글(삭제)을 위해 특정 유저와 리뷰로 좋아요 찾기
    Optional<ReviewLike> findByUserAndReview(User user, Review review);
    // 리뷰 상세나 피드에서 좋아요 개수 보줄 때 사용
    long countByReview(Review review);
    // 내가 이미 좋아요를 눌렀는지 확인 (피드 조회용)
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
}
