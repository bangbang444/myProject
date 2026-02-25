package bangbang.gourmet.review.entity;

import bangbang.gourmet.common.entity.BaseEntity;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double rating;   // UI: 5.0, 4.0 등

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // 리뷰 본문

    // 리뷰 이미지는 별도 엔티티(1:N)로 분리
    @OneToMany(mappedBy = "review")
    private final List<ReviewImage> images = new ArrayList<>();

    @Builder
    public Review(Restaurant restaurant, User user, Double rating, String content) {
        this.restaurant = restaurant;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }

    public void addReviewImage(String imageUrl) {
        ReviewImage reviewImage = new ReviewImage(this, imageUrl);
        this.images.add(reviewImage);
    }
}
