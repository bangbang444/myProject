package bangbang.gourmet.social.concurrency;

import bangbang.gourmet.common.domain.Role;
import bangbang.gourmet.common.domain.SocialProvider;
import bangbang.gourmet.social.repository.FollowRepository;
import bangbang.gourmet.social.service.FollowService;
import bangbang.gourmet.user.entity.User;
import bangbang.gourmet.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class FollowConcurrencyTest {

    @Autowired private FollowService followService;
    @Autowired private UserRepository userRepository;
    @Autowired private FollowRepository followRepository;

    private Long followerId;
    private Long followingId;

    @BeforeEach
    void setUp() {
        User follower = userRepository.save(User.builder()
                .email("follower-" + System.nanoTime() + "@test.com")
                .nickname("팔로워")
                .role(Role.ROLE_USER)
                .provider(SocialProvider.KAKAO)
                .providerId("follower-" + System.nanoTime())
                .build());
        followerId = follower.getId();

        User following = userRepository.save(User.builder()
                .email("following-" + System.nanoTime() + "@test.com")
                .nickname("팔로잉")
                .role(Role.ROLE_USER)
                .provider(SocialProvider.KAKAO)
                .providerId("following-" + System.nanoTime())
                .build());
        followingId = following.getId();
    }

    @Test
    @Disabled("동시성 문제 육안 확인용 — 수동 실행 시에만 활성화")
    @DisplayName("[동시성 확인] 동시 팔로우 요청 시 중복 팔로우 레코드 검증")
    void 동시_팔로우_요청시_중복_팔로우_검증() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    followService.toggleFollow(followerId, followingId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("충돌 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        long followCount = followRepository.count();

        System.out.println("\n========== 팔로우 동시성 테스트 결과 ==========");
        System.out.println("요청 수              : " + threadCount);
        System.out.println("성공 수              : " + successCount.get());
        System.out.println("실패(충돌) 수        : " + failCount.get());
        System.out.println("DB follow 레코드 수  : " + followCount);
        System.out.println("중복 발생 여부       : " + (followCount > 1 ? "발생 (" + followCount + "개)" : "없음"));
        System.out.println("================================================\n");
    }
}