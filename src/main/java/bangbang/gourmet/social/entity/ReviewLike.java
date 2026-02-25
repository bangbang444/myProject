package bangbang.gourmet.social.entity;

import bangbang.gourmet.common.entity.BaseEntity;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "review_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_like_user_review",
                        columnNames = {"user_id", "review_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    @Builder
    public ReviewLike(User user, Review review) {
        this.user = user;
        this.review = review;
    }
}
