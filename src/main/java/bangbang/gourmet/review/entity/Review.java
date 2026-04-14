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
    private Double tasteRating;

    @Column(nullable = false)
    private Double atmosphereRating;

    @Column(nullable = false)
    private Double serviceRating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;  // 리뷰 본문

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Boolean isPublic = false;

    // 리뷰 이미지는 별도 엔티티(1:N)로 분리
    @OneToMany(mappedBy = "review")
    private final List<ReviewImage> images = new ArrayList<>();

    @Builder
    public Review(Restaurant restaurant, User user, Double tasteRating, Double atmosphereRating, Double serviceRating, String content, String category, Boolean isPublic) {
        this.restaurant = restaurant;
        this.user = user;
        this.tasteRating = tasteRating;
        this.atmosphereRating = atmosphereRating;
        this.serviceRating = serviceRating;
        this.content = content;
        this.category = category;
        this.isPublic = isPublic != null ? isPublic : false;
    }

    public Double getAverageRating() {
        return (tasteRating + atmosphereRating + serviceRating) / 3.0;
    }

    public void addReviewImage(String imageUrl) {
        ReviewImage reviewImage = ReviewImage.create(this, imageUrl);
        this.images.add(reviewImage);
    }

    public void update(String content, Double tasteRating, Double atmosphereRating, Double serviceRating) {
        this.content = content;
        this.tasteRating = tasteRating;
        this.atmosphereRating = atmosphereRating;
        this.serviceRating = serviceRating;
    }
}
