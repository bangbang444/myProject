package bangbang.gourmet.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("loadtest")
@RequiredArgsConstructor
public class LoadTestDataLoader implements ApplicationRunner {

    private static final int USER_COUNT = 1_000;
    private static final int RESTAURANT_COUNT = 100;
    private static final int REVIEW_COUNT = 1_000_000;
    private static final int BATCH_SIZE = 5_000;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        if (isDataAlreadyLoaded()) {
            log.info("[LoadTest] 시드 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("[LoadTest] 시드 데이터 삽입 시작");
        long start = System.currentTimeMillis();

        seedUsers();
        seedRestaurants();
        seedFollows();
        seedReviews();

        long elapsed = (System.currentTimeMillis() - start) / 1000;
        log.info("[LoadTest] 시드 데이터 삽입 완료 - 소요 시간: {}초", elapsed);
    }

    private boolean isDataAlreadyLoaded() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email LIKE 'loadtest%'", Integer.class);
        return count != null && count > 0;
    }

    private void seedUsers() {
        log.info("[LoadTest] 유저 {}명 삽입 중...", USER_COUNT);
        String sql = "INSERT INTO users (email, role, provider, provider_id, nickname, created_date, last_modified_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batch = new ArrayList<>(USER_COUNT);
        for (int i = 1; i <= USER_COUNT; i++) {
            batch.add(new Object[]{"loadtest" + i + "@test.com", "ROLE_USER", "KAKAO", "loadtest-" + i, "테스트유저" + i, now, now, "ACTIVE"});
        }
        jdbcTemplate.batchUpdate(sql, batch);
        log.info("[LoadTest] 유저 {}명 삽입 완료", USER_COUNT);
    }

    private void seedRestaurants() {
        log.info("[LoadTest] 식당 {}개 삽입 중...", RESTAURANT_COUNT);
        String sql = "INSERT INTO restaurant (restaurant_name, address, naver_place_id, sido, sigungu, dong, address_full, average_rating, review_count, latitude, longitude, main_category, created_date, last_modified_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Object[]> batch = new ArrayList<>(RESTAURANT_COUNT);
        for (int i = 1; i <= RESTAURANT_COUNT; i++) {
            batch.add(new Object[]{"테스트식당" + i, "서울 테스트구 " + i, "loadtest-place-" + i, "서울특별시", "테스트구", "테스트동", "서울특별시 테스트구 테스트동 " + i, 0.0, 0, 37.5 + (i * 0.001), 127.0 + (i * 0.001), "한식", now, now, "ACTIVE"});
        }
        jdbcTemplate.batchUpdate(sql, batch);
        log.info("[LoadTest] 식당 {}개 삽입 완료", RESTAURANT_COUNT);
    }

    private void seedFollows() {
        Long userId1 = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'loadtest1@test.com'", Long.class);
        if (userId1 == null) return;

        log.info("[LoadTest] 팔로우 관계 삽입 중...");
        String sql = "INSERT INTO follow (follower_id, following_id, created_date, last_modified_date, status) VALUES (?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Long> followingIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE email LIKE 'loadtest%' AND email != 'loadtest1@test.com'", Long.class);

        List<Object[]> batch = new ArrayList<>();
        for (Long followingId : followingIds) {
            batch.add(new Object[]{userId1, followingId, now, now, "ACTIVE"});
            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }
        log.info("[LoadTest] 팔로우 관계 {}개 삽입 완료", followingIds.size());
    }

    private void seedReviews() {
        log.info("[LoadTest] 리뷰 {}개 삽입 중...", REVIEW_COUNT);
        String sql = "INSERT INTO review (restaurant_id, user_id, taste_rating, atmosphere_rating, service_rating, content, category, is_public, created_date, last_modified_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        List<Long> userIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE email LIKE 'loadtest%' AND email != 'loadtest1@test.com'", Long.class);
        List<Long> restaurantIds = jdbcTemplate.queryForList(
                "SELECT restaurant_id FROM restaurant WHERE naver_place_id LIKE 'loadtest%'", Long.class);

        int userSize = userIds.size();
        int restaurantSize = restaurantIds.size();

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < REVIEW_COUNT; i++) {
            Long userId = userIds.get(i % userSize);
            Long restaurantId = restaurantIds.get(i % restaurantSize);
            double rating = 3.0 + (i % 3);
            batch.add(new Object[]{restaurantId, userId, rating, rating, rating, "부하테스트 리뷰 내용입니다. #" + (i + 1), "한식", true, now, now, "ACTIVE"});

            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batch);
                batch.clear();
                log.info("[LoadTest] 리뷰 삽입 중... {}/{}", i + 1, REVIEW_COUNT);
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
        }
        log.info("[LoadTest] 리뷰 {}개 삽입 완료", REVIEW_COUNT);
    }
}
