package bangbang.gourmet.review.repository;

import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    long countByReview(Review review);

    List<Comment> findAllByReviewOrderByCreatedDate(Review review);
}
