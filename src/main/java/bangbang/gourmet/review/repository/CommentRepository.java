package bangbang.gourmet.review.repository;

import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    long countByReview(Review review);

    // TODO: fetch join
    List<Comment> findAllByReviewOrderByCreatedDate(Review review);

    @Query("select c from Comment c join fetch c.user where c.review = :review order by c.createdDate")
    List<Comment> findAllByReviewWithUserOrderByCreatedDate(@Param("review") Review review);
}
