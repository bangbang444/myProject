package bangbang.gourmet.review.concurrency;

import bangbang.gourmet.common.domain.Role;
import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.global.s3.S3Service;
import org.junit.jupiter.api.Disabled;
import bangbang.gourmet.restaurant.entity.Restaurant;
import bangbang.gourmet.restaurant.repository.RestaurantRepository;
import bangbang.gourmet.review.dto.ReviewCreateRequest;
import bangbang.gourmet.review.service.ReviewService;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ReviewConcurrencyTest {

    @Autowired private ReviewService reviewService;
    @Autowired private RestaurantRepository restaurantRepository;
    @Autowired private UserRepository userRepository;
    @MockitoBean private S3Service s3Service;

    private Long restaurantId;
    private Long userId;

    @BeforeEach
    void setUp() {
        given(s3Service.uploadImage(any(), anyString(), anyString())).willReturn("mock-key");

        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .restaurantName("동시성 테스트 식당")
                .address("서울시 강남구")
                .naverPlaceId("concurrency-test-" + System.nanoTime())
                .mainCategory("한식")
                .averageRating(0.0)
                .reviewCount(0)
                .latitude(37.5)
                .longitude(127.0)
                .build());
        restaurantId = restaurant.getRestaurantId();

        User user = userRepository.save(User.builder()
                .email("concurrency-" + System.nanoTime() + "@test.com")
                .nickname("테스터")
                .role(Role.ROLE_USER)
                .provider(SocialProvider.KAKAO)
                .providerId("concurrency-" + System.nanoTime())
                .build());
        userId = user.getId();
    }

    @Test
    @Disabled("동시성 문제 육안 확인용 — 수동 실행 시에만 활성화")
    @DisplayName("[동시성 확인] 10명이 동시에 리뷰 작성 시 reviewCount가 정확히 10이어야 한다")
    void 동시_리뷰_작성시_reviewCount_정합성_검증() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);  // 동시에 출발
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ReviewCreateRequest request = new ReviewCreateRequest(5.0, 5.0, 5.0, "동시 리뷰", "한식", true);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    reviewService.createReview(restaurantId, userId, request, List.of());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("충돌 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 동시 출발
        doneLatch.await();      // 모든 스레드 완료 대기
        executor.shutdown();

        Restaurant result = restaurantRepository.findById(restaurantId).orElseThrow();

        System.out.println("\n========== 동시성 테스트 결과 ==========");
        System.out.println("요청 수          : " + threadCount);
        System.out.println("성공 수          : " + successCount.get());
        System.out.println("실패(충돌) 수    : " + failCount.get());
        System.out.println("최종 reviewCount : " + result.getReviewCount());
        System.out.println("Lost Update 여부  : " + (result.getReviewCount() < successCount.get() ? "발생" : "없음"));
        System.out.println("==========================================\n");
    }
}