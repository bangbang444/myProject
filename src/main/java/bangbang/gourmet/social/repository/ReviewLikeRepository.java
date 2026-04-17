package bangbang.gourmet.social.repository;

import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.social.entity.ReviewLike;
import bangbang.gourmet.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByUserAndReview(User user, Review review);
    long countByReview(Review review);
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    @Query("select rl.review.id, count(rl) from ReviewLike rl where rl.review.id in :reviewIds group by rl.review.id")
    List<Object[]> countByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @Query("select rl.review.id from ReviewLike rl where rl.user.id = :userId and rl.review.id in :reviewIds")
    List<Long> findLikedReviewIdsByUserIdAndReviewIds(@Param("userId") Long userId, @Param("reviewIds") List<Long> reviewIds);
}
