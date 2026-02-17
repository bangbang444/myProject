package bangbang.gourmet.common.security.jwt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

import static bangbang.gourmet.common.security.jwt.JwtConstants.*;

/**
 * Redis에 저장되는 리프레시 토큰 엔티티
 * User ID를 key로 사용하여 사용자당 하나의 토큰만 유지
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refresh_token", timeToLive = REFRESH_TOKEN_EXPIRE_TIME_SECONDS) // 14일 (초 단위)
public class RefreshToken {

    /**
     * User ID를 Primary Key로 사용
     * 한 사용자당 하나의 토큰만 유지 (재로그인 시 덮어쓰기)
     */
    @Id
    private Long userId;

    /**
     * 리프레시 토큰 값
     * 인덱스를 설정하여 토큰 값으로 역조회 가능 (향후 /refresh 엔드포인트용)
     */
    @Indexed
    private String token;

    /**
     * 토큰 생성 시간 (디버깅 및 추적용)
     */
    private LocalDateTime createdAt;
}