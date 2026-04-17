package bangbang.gourmet.review.query;

import bangbang.gourmet.common.domain.Role;
import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.entity.Comment;
import bangbang.gourmet.review.entity.Review;
import bangbang.gourmet.review.repository.CommentRepository;
import bangbang.gourmet.review.repository.ReviewRepository;
import bangbang.gourmet.social.entity.Follow;
import bangbang.gourmet.social.entity.ReviewLike;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.social.repository.ReviewLikeRepository;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ReviewFeedQueryTest {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private ReviewLikeRepository reviewLikeRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RestaurantRepository restaurantRepository;

    @PersistenceContext
    private EntityManager em;

    private User viewer;
    private int reviewCount;

    @BeforeEach
    void setUp() {
        viewer = userRepository.save(User.builder()
                .email("viewer@test.com").nickname("뷰어")
                .role(Role.ROLE_USER).provider(SocialProvider.KAKAO).providerId("viewer-1")
                .build());

        User author1 = userRepository.save(User.builder()
                .email("author1@test.com").nickname("작성자1")
                .role(Role.ROLE_USER).provider(SocialProvider.KAKAO).providerId("author-1")
                .build());

        User author2 = userRepository.save(User.builder()
                .email("author2@test.com").nickname("작성자2")
                .role(Role.ROLE_USER).provider(SocialProvider.KAKAO).providerId("author-2")
                .build());

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .restaurantName("테스트식당").address("서울시 강남구")
                .naverPlaceId("test-place-1").mainCategory("한식")
                .averageRating(0.0).reviewCount(0)
                .latitude(37.5).longitude(127.0)
                .build());

        followRepository.save(Follow.builder().follower(viewer).following(author1).build());
        followRepository.save(Follow.builder().follower(viewer).following(author2).build());

        // author1 리뷰 2개, author2 리뷰 1개
        Review r1 = reviewRepository.save(createReview(author1, restaurant, 1));
        Review r2 = reviewRepository.save(createReview(author1, restaurant, 2));
        Review r3 = reviewRepository.save(createReview(author2, restaurant, 3));
        reviewCount = 3;

        reviewLikeRepository.save(ReviewLike.builder().user(viewer).review(r1).build());
        commentRepository.save(Comment.builder().content("맛있어요").user(viewer).review(r1).build());
        commentRepository.save(Comment.builder().content("또 올게요").user(viewer).review(r2).build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("피드 조회 쿼리 수 확인 — N+1 검증")
    void countFeedQueries() {
        SessionFactory sf = em.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sf.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // ReviewFeedService.getFollowerFeed() 와 동일한 흐름 (배치 조회)
        List<Long> reviewIds = reviewRepository.findFeedIdsByFollowerId(viewer.getId(), PageRequest.of(0, 100)); // 쿼리 1
        List<Review> reviews = reviewRepository.findAllWithDetailsByIds(reviewIds);                              // 쿼리 2

        reviewLikeRepository.countByReviewIds(reviewIds);                                                        // 쿼리 3
        commentRepository.countByReviewIds(reviewIds);                                                           // 쿼리 4
        reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(viewer.getId(), reviewIds);                  // 쿼리 5

        long totalQueries = stats.getPrepareStatementCount();
        int expectedFixed = 5; // feedIds + details(user+restaurant+images) + likeCount배치 + commentCount배치 + isLiked배치

        System.out.println("\n========== 피드 조회 쿼리 분석 ==========");
        System.out.println("리뷰 수    : " + reviews.size());
        System.out.println("총 쿼리 수 : " + totalQueries);
        System.out.println("예상 쿼리  : " + expectedFixed + " (고정)");
        System.out.println("==========================================\n");

        assertThat(reviews).hasSize(reviewCount);
        assertThat(totalQueries).isEqualTo(expectedFixed);
    }

    private Review createReview(User author, Restaurant restaurant, int idx) {
        return Review.builder()
                .user(author).restaurant(restaurant)
                .content("리뷰 내용 " + idx)
                .tasteRating(4.0).atmosphereRating(4.0).serviceRating(4.0)
                .category("한식").isPublic(true)
                .build();
    }
}
