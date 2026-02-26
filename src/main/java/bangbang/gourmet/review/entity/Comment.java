package bangbang.gourmet.review.entity;

import bangbang.gourmet.common.entity.BaseEntity;
import bangbang.gourmet.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500) // 댓글 내용은 최대 500자 정도로 제한
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Builder
    public Comment(String content, User user, Review review) {
        this.content = content;
        this.user = user;
        this.review = review;
    }

    public void update(String content) {
        this.content = content;
    }
}
