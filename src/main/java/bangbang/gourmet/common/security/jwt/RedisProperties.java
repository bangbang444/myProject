package bangbang.gourmet.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis 연결 정보를 외부 설정 파일(application.yml)에서 주입받는 Properties 클래스
 * JwtProperties와 동일한 패턴 사용
 */
@ConfigurationProperties("spring.data.redis")
public record RedisProperties(
    String host,
    int port
) {}