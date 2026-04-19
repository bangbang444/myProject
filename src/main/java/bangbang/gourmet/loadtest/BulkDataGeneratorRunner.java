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
import java.util.*;

/**
 * 롱테일 분포 기반 대규모 테스트 데이터 생성기 (profile: loadtest)
 *
 * 실행: --spring.profiles.active=loadtest
 *
 * 목표:
 *   User       25,000명  - 80:15:5 법칙 (슈퍼유저 5%가 리뷰 ~40% 생산)
 *   Restaurant  1,000,000개 - 3-티어 Zipf 근사 (상위 1%에 리뷰 집중)
 *   Review      1,000,000건 - 유저 활동성 × 식당 인기도 교차 분포
 *   Follow        300,000건 - Power Law (인플루언서 250명에 팔로워 집중)
 */
@Slf4j
@Component
@Profile("loadtest")
@RequiredArgsConstructor
public class BulkDataGeneratorRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Random rng = new Random(42); // 고정 시드 → 재현 가능

    // ── 목표 수치 ──────────────────────────────────────────────────────────────
    private static final int USER_COUNT       = 25_000;
    private static final int RESTAURANT_COUNT = 1_000_000;
    private static final int REVIEW_COUNT     = 1_000_000;
    private static final int FOLLOW_COUNT     = 300_000;
    private static final int BATCH_SIZE       = 2_000;

    // ── 유저 티어 경계 (롱테일 80:15:5) ───────────────────────────────────────
    private static final int TIER1_END = 1_250;             // 상위 5%  슈퍼유저
    private static final int TIER2_END = TIER1_END + 3_750; // 다음 15% 활성유저
    // TIER3: 나머지 80% (5,001 ~ 25,000)

    // 티어별 리뷰 할당 범위  →  총합 ≈ 968K (T1 387K + T2 281K + T3 300K)
    private static final int T1_MIN = 260, T1_MAX = 360;
    private static final int T2_MIN =  55, T2_MAX =  95;
    private static final int T3_MIN =   8, T3_MAX =  22;

    // ── 식당 Zipf 3-티어 경계 ─────────────────────────────────────────────────
    // HOT 1만 개(상위 1%)가 리뷰의 ~27% 수용 → 식당 1개당 평균 27건
    // WARM 9만 개(상위 10%)가 ~24% → 평균 2.7건
    // COLD 90만 개(나머지)가 ~49% → 평균 0.5건
    private static final int HOT_END  =  10_000;
    private static final int WARM_END = 100_000;

    // ── 팔로우 Power Law 티어 ──────────────────────────────────────────────────
    // 인플루언서 250명이 팔로우의 50% 수용 → 1인당 평균 600명 팔로워
    // 인기유저  2,250명이 20% 수용         → 1인당 평균 26명
    // 일반유저  나머지가 30% 수용          → 1인당 평균 3명
    private static final int INFLUENCER_END =   250;
    private static final int POPULAR_END    = 2_500;

    // ── 한국어 리소스 풀 ───────────────────────────────────────────────────────
    private static final String[] SIDO = {
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    };
    private static final String[][] SIGUNGU = {
        {"강남구", "강북구", "마포구", "송파구", "서초구", "종로구", "용산구"},
        {"해운대구", "남구", "동래구", "부산진구"},
        {"수성구", "달서구", "중구"},
        {"연수구", "남동구", "부평구"},
        {"서구", "북구", "광산구"},
        {"유성구", "서구", "중구"},
        {"남구", "울주군"},
        {"세종시"},
        {"수원시", "성남시", "고양시", "용인시", "부천시", "화성시"},
        {"춘천시", "원주시", "강릉시"},
        {"청주시", "충주시"},
        {"천안시", "아산시"},
        {"전주시", "군산시"},
        {"목포시", "여수시"},
        {"포항시", "경주시", "구미시"},
        {"창원시", "김해시", "진주시"},
        {"제주시", "서귀포시"}
    };
    private static final String[] CATEGORIES = {
        "한식", "중식", "일식", "양식", "카페", "디저트", "분식", "패스트푸드", "베이커리", "이탈리안"
    };
    private static final String[] RESTAURANT_SUFFIXES = {
        "식당", "맛집", "레스토랑", "하우스", "키친", "다이닝", "비스트로", "그릴"
    };
    private static final String[] REVIEW_CONTENTS = {
        "음식이 정말 맛있었습니다. 재방문 의사 있어요!",
        "가격 대비 훌륭한 음식이었습니다.",
        "분위기가 아늑하고 서비스가 친절했습니다.",
        "주차 공간이 넓어서 좋았어요.",
        "조용한 분위기에서 식사하기 좋습니다.",
        "양이 많고 맛도 훌륭합니다.",
        "친구들과 함께 가기 좋은 곳이에요.",
        "데이트 코스로 추천드립니다.",
        "점심 특선이 가성비 최고예요.",
        "사장님이 너무 친절하십니다.",
    };

    // ── 진입점 ────────────────────────────────────────────────────────────────

    @Override
    public void run(ApplicationArguments args) {
        if (isDataAlreadyLoaded()) {
            log.info("[DataGen] loadtest 데이터가 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("========== [DataGen] 대규모 데이터 생성 시작 ==========");
        log.info("[DataGen] 목표: User {}명 / Restaurant {}개 / Review {}건 / Follow {}건",
            USER_COUNT, RESTAURANT_COUNT, REVIEW_COUNT, FOLLOW_COUNT);

        long start = System.currentTimeMillis();
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("SET UNIQUE_CHECKS = 0");

        try {
            seedUsers();
            seedRestaurants();
            seedReviews();
            updateRestaurantStats();
            seedFollows();
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            jdbcTemplate.execute("SET UNIQUE_CHECKS = 1");
        }

        log.info("========== [DataGen] 완료 (총 {}초) ==========",
            (System.currentTimeMillis() - start) / 1000);
    }

    // ── 중복 생성 방지 ────────────────────────────────────────────────────────

    private boolean isDataAlreadyLoaded() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email LIKE 'loadtest%'", Integer.class);
        return count != null && count > 0;
    }

    // ── User 생성 ─────────────────────────────────────────────────────────────

    private void seedUsers() {
        log.info("[DataGen/Users] 생성 시작: {}명", USER_COUNT);
        String sql = """
            INSERT INTO users (email, role, provider, provider_id, nickname, bio,
                created_date, last_modified_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        LocalDateTime base = LocalDateTime.now();

        for (int i = 1; i <= USER_COUNT; i++) {
            Timestamp created = ts(base.minusDays(rng.nextInt(365 * 2)));
            batch.add(new Object[]{
                "loadtest_user_" + i + "@test.com",
                "ROLE_USER",
                "KAKAO",
                "loadtest_kakao_" + i,
                tierNickname(i),
                i % 5 == 0 ? tierNickname(i) + "입니다!" : null,
                created, created, "ACTIVE"
            });
            flush(sql, batch, false);
        }
        flush(sql, batch, true);
        log.info("[DataGen/Users] 완료: {}명", USER_COUNT);
    }

    // ── Restaurant 생성 ───────────────────────────────────────────────────────

    private void seedRestaurants() {
        log.info("[DataGen/Restaurants] 생성 시작: {}개", RESTAURANT_COUNT);
        String sql = """
            INSERT INTO restaurant (version, restaurant_name, address, naver_place_id,
                sido, sigungu, dong, address_full,
                average_rating, review_count, latitude, longitude,
                main_category, description, phone_number,
                created_date, last_modified_date, status)
            VALUES (0, ?, ?, ?, ?, ?, ?, ?, 0.0, 0, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        LocalDateTime base = LocalDateTime.now();

        for (int i = 1; i <= RESTAURANT_COUNT; i++) {
            int sidoIdx    = rng.nextInt(SIDO.length);
            String sido    = SIDO[sidoIdx];
            String sigungu = SIGUNGU[sidoIdx][rng.nextInt(SIGUNGU[sidoIdx].length)];
            String dong    = "테스트동 " + (rng.nextInt(999) + 1) + "번지";
            String category = CATEGORIES[rng.nextInt(CATEGORIES.length)];
            String name    = category + " " + RESTAURANT_SUFFIXES[rng.nextInt(RESTAURANT_SUFFIXES.length)] + " " + i;
            double lat     = 33.0 + rng.nextDouble() * 5.0;   // 한국 위도 범위
            double lon     = 126.0 + rng.nextDouble() * 3.0;  // 한국 경도 범위
            Timestamp created = ts(base.minusDays(rng.nextInt(365 * 3)));

            batch.add(new Object[]{
                name,
                sido + " " + sigungu + " " + dong,
                "loadtest_place_" + i,
                sido, sigungu, dong,
                sido + " " + sigungu + " " + dong,
                lat, lon,
                category,
                i % 10 == 0 ? name + " 상세 설명입니다." : null,
                i % 7 == 0 ? "02-" + String.format("%04d", rng.nextInt(10_000))
                                 + "-" + String.format("%04d", rng.nextInt(10_000)) : null,
                created, created, "ACTIVE"
            });

            if (i % 100_000 == 0) log.info("[DataGen/Restaurants] {}개 처리 중...", i);
            flush(sql, batch, false);
        }
        flush(sql, batch, true);
        log.info("[DataGen/Restaurants] 완료: {}개", RESTAURANT_COUNT);
    }

    // ── Review 생성 ───────────────────────────────────────────────────────────

    private void seedReviews() {
        log.info("[DataGen/Reviews] 생성 시작: {}건 (롱테일 80:15:5 + Zipf 식당 분포)", REVIEW_COUNT);

        int[]  quotas   = buildUserReviewQuotas();
        long   userStart = queryFirstUserId();
        long   restStart = queryFirstRestaurantId();

        String sql = """
            INSERT INTO review (restaurant_id, user_id, taste_rating, atmosphere_rating, service_rating,
                content, category, is_public, created_date, last_modified_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        LocalDateTime base = LocalDateTime.now();
        int totalInserted = 0;

        for (int ui = 1; ui <= USER_COUNT && totalInserted < REVIEW_COUNT; ui++) {
            int count  = Math.min(quotas[ui], REVIEW_COUNT - totalInserted);
            long userId = userStart + ui - 1;

            for (int j = 0; j < count; j++) {
                long restaurantId = restStart + sampleRestaurantOffset();
                Timestamp created = ts(base.minusDays(rng.nextInt(365 * 2)));

                batch.add(new Object[]{
                    restaurantId, userId,
                    randomRating(), randomRating(), randomRating(),
                    REVIEW_CONTENTS[rng.nextInt(REVIEW_CONTENTS.length)],
                    CATEGORIES[rng.nextInt(CATEGORIES.length)],
                    rng.nextDouble() > 0.4, // 60% 공개
                    created, created, "ACTIVE"
                });
                flush(sql, batch, false);
                totalInserted++;
            }

            if (ui % 5_000 == 0) {
                log.info("[DataGen/Reviews] 유저 {}까지 처리, 누적 {}건", ui, totalInserted);
            }
        }
        flush(sql, batch, true);
        log.info("[DataGen/Reviews] 완료: {}건", totalInserted);
    }

    // ── Restaurant 통계 업데이트 ──────────────────────────────────────────────

    private void updateRestaurantStats() {
        log.info("[DataGen/Stats] 식당 리뷰 수 / 평균 평점 일괄 업데이트 중...");
        jdbcTemplate.execute("""
            UPDATE restaurant r
            JOIN (
                SELECT restaurant_id,
                       COUNT(*)                                                        AS cnt,
                       AVG((taste_rating + atmosphere_rating + service_rating) / 3.0) AS avg_r
                FROM review
                WHERE status = 'ACTIVE'
                GROUP BY restaurant_id
            ) stats ON r.restaurant_id = stats.restaurant_id
            SET r.review_count   = stats.cnt,
                r.average_rating = stats.avg_r
            """);
        log.info("[DataGen/Stats] 완료");
    }

    // ── Follow 생성 ───────────────────────────────────────────────────────────

    private void seedFollows() {
        log.info("[DataGen/Follows] 생성 시작: {}건 (Power Law 인플루언서 집중)", FOLLOW_COUNT);

        long userStart = queryFirstUserId();
        String sql = """
            INSERT IGNORE INTO follow (follower_id, following_id, created_date, last_modified_date, status)
            VALUES (?, ?, ?, ?, ?)
            """;

        // (follower_offset, following_offset) 중복 방지
        // 인코딩: followerOffset * 30_000 + followingOffset  (max offset = 24_999 < 30_000)
        Set<Long> pairs = new HashSet<>(FOLLOW_COUNT * 2);
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        LocalDateTime base = LocalDateTime.now();
        int attempts = 0;

        while (pairs.size() < FOLLOW_COUNT && attempts < FOLLOW_COUNT * 6) {
            int followingOffset = sampleFollowTargetOffset();
            int followerOffset  = rng.nextInt(USER_COUNT);
            if (followerOffset == followingOffset) { attempts++; continue; }

            long key = (long) followerOffset * 30_000L + followingOffset;
            if (pairs.add(key)) {
                Timestamp created = ts(base.minusDays(rng.nextInt(365 * 2)));
                batch.add(new Object[]{
                    userStart + followerOffset,
                    userStart + followingOffset,
                    created, created, "ACTIVE"
                });
                flush(sql, batch, false);
            }
            attempts++;
        }
        flush(sql, batch, true);
        log.info("[DataGen/Follows] 완료: {}건 (시도: {}회)", pairs.size(), attempts);
    }

    // ── 분포 알고리즘 ─────────────────────────────────────────────────────────

    /**
     * 80:15:5 법칙 기반 유저별 리뷰 할당량 사전 계산
     *   T1 슈퍼유저  1,250명 × avg 310건 ≈  387,500건 (40%)
     *   T2 활성유저  3,750명 × avg  75건 ≈  281,250건 (29%)
     *   T3 일반유저 20,000명 × avg  15건 ≈  300,000건 (31%)
     */
    private int[] buildUserReviewQuotas() {
        int[] q = new int[USER_COUNT + 1];
        for (int i = 1;            i <= TIER1_END;  i++) q[i] = T1_MIN + rng.nextInt(T1_MAX - T1_MIN + 1);
        for (int i = TIER1_END+1;  i <= TIER2_END;  i++) q[i] = T2_MIN + rng.nextInt(T2_MAX - T2_MIN + 1);
        for (int i = TIER2_END+1;  i <= USER_COUNT; i++) q[i] = T3_MIN + rng.nextInt(T3_MAX - T3_MIN + 1);
        return q;
    }

    /**
     * 3-티어 Zipf 근사 식당 오프셋 샘플링
     *   HOT  (상위 1%,  1만 개): 27% 확률 → 1개당 평균 ~27건
     *   WARM (상위 10%, 9만 개): 24% 확률 → 1개당 평균 ~2.7건
     *   COLD (나머지,  90만 개): 49% 확률 → 1개당 평균 ~0.5건
     */
    private int sampleRestaurantOffset() {
        double r = rng.nextDouble();
        if (r < 0.27) return rng.nextInt(HOT_END);
        if (r < 0.51) return HOT_END + rng.nextInt(WARM_END - HOT_END);
        return WARM_END + rng.nextInt(RESTAURANT_COUNT - WARM_END);
    }

    /**
     * Power Law 팔로우 대상 오프셋 샘플링
     *   인플루언서   250명: 50% → 1인당 평균 ~600명 팔로워
     *   인기유저   2,250명: 20% → 1인당 평균 ~26명 팔로워
     *   일반유저  22,500명: 30% → 1인당 평균 ~4명 팔로워
     */
    private int sampleFollowTargetOffset() {
        double r = rng.nextDouble();
        if (r < 0.50) return rng.nextInt(INFLUENCER_END);
        if (r < 0.70) return INFLUENCER_END + rng.nextInt(POPULAR_END - INFLUENCER_END);
        return rng.nextInt(USER_COUNT);
    }

    /** 현실적 평점 분포: 1~5점 중 3~5점에 집중 */
    private double randomRating() {
        double[] opts    = {1.0, 2.0, 3.0, 3.5, 4.0, 4.5, 5.0};
        double[] weights = {0.03, 0.05, 0.10, 0.17, 0.25, 0.22, 0.18};
        double r = rng.nextDouble(), cum = 0;
        for (int i = 0; i < opts.length; i++) {
            cum += weights[i];
            if (r < cum) return opts[i];
        }
        return 4.0;
    }

    // ── 공통 유틸 ─────────────────────────────────────────────────────────────

    private String tierNickname(int i) {
        if (i <= TIER1_END) return "슈퍼맛집러" + i;
        if (i <= TIER2_END) return "활동가" + i;
        return "사용자" + i;
    }

    private long queryFirstUserId() {
        Long id = jdbcTemplate.queryForObject(
            "SELECT MIN(id) FROM users WHERE email LIKE 'loadtest%'", Long.class);
        return id != null ? id : 1L;
    }

    private long queryFirstRestaurantId() {
        Long id = jdbcTemplate.queryForObject(
            "SELECT MIN(restaurant_id) FROM restaurant WHERE naver_place_id LIKE 'loadtest%'", Long.class);
        return id != null ? id : 1L;
    }

    private Timestamp ts(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt);
    }

    private void flush(String sql, List<Object[]> batch, boolean force) {
        if ((force || batch.size() >= BATCH_SIZE) && !batch.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batch);
            batch.clear();
        }
    }
}