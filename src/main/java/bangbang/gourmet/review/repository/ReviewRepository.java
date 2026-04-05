package bangbang.gourmet.review.repository;

import bangbang.gourmet.review.dto.UserReviewStats;
import bangbang.gourmet.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("select distinct r from Review r " +
            "join fetch r.user " +
            "left join fetch r.images " +
            "where r.restaurant.restaurantId = :restaurantId " +
            "order by r.createdDate desc")
    List<Review> findTop2ByRestaurantIdWithUserAndImages(@Param("restaurantId") Long restaurantId, Pageable pageable);


    @Query("select distinct r from Review r " +
            "left join fetch r.images " +
            "join fetch r.user " +
            "where r.restaurant.restaurantId = :restaurantId " +
            "order by r.createdDate desc")
    List<Review> findAllByRestaurantIdWithImages(@Param("restaurantId") Long restaurantId);

    @Query("select distinct r from Review r " +
            "join fetch r.user " +
            "join fetch r.restaurant " +
            "left join fetch r.images " +
            "where r.user.id in :userIds " + // 넘겨받은 팔로잉 ID 리스트에 포함된 것만!
            "order by r.createdDate desc")
    List<Review> findAllByUserIds(@Param("userIds") List<Long> userIds);

    @Query("select distinct r from Review r " +
            "join fetch r.user " +
            "join fetch r.restaurant " +
            "left join fetch r.images " +
            "where r.id = :reviewId")
    Optional<Review> findByIdWithDetails(@Param("reviewId") Long reviewId);

    @Query("select new bangbang.gourmet.review.dto.UserReviewStats(r.user.id, COUNT(r), AVG((r.tasteRating + r.atmosphereRating + r.serviceRating) / 3.0)) " +
            "from Review r " +
            "where r.user.id in :userIds " +
            "group by r.user.id")
    List<UserReviewStats> findUserReviewStatsByUserIds(@Param("userIds") List<Long> userIds);

    @Query("select distinct r from Review r " +
            "join fetch r.user " +
            "join fetch r.restaurant " +
            "left join fetch r.images " +
            "where r.id = :reviewId")
    Optional<Review> findByIdWithDetails(@Param("reviewId") Long reviewId);
}
